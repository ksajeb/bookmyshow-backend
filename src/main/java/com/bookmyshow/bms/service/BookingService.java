package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.*;
import com.bookmyshow.bms.entity.*;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.exception.SeatUnavailableException;
import com.bookmyshow.bms.repository.BookingRepository;
import com.bookmyshow.bms.repository.ShowRepository;
import com.bookmyshow.bms.repository.ShowSeatRepository;
import com.bookmyshow.bms.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;

    private final ShowRepository showRepository;

    private final ShowSeatRepository showSeatRepository;

    private final BookingRepository bookingRepository;

    private final ModelMapper modelMapper;

    @Transactional
    public BookingDto createBooking(@Valid BookingRequestDto bookingRequest) {
        User user=userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(()->new ResourceNotFoundException("User Not Found"));

        Show show=showRepository.findById(bookingRequest.getShowId())
                .orElseThrow(()->new ResourceNotFoundException("Show Not Found"));

        List<ShowSeat> selectedSeats=showSeatRepository.findAllById(bookingRequest.getSeatIds());

        for(ShowSeat seat:selectedSeats){
            if (!"AVAILABLE".equals(seat.getStatus())){
                throw new SeatUnavailableException("Seat "+seat.getSeat().getSeatNumber()+" is not available");
            }
            seat.setStatus("LOCKED");
        }
        showSeatRepository.saveAll(selectedSeats);

        Double totalAmount=selectedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();

        Payment payment=new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentMethod(bookingRequest.getPaymentMethod());
        payment.setStatus("SUCCESS");
        payment.setTransactionId(UUID.randomUUID().toString());

        Booking booking=new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRM");
        booking.setTotalAmount(totalAmount);
        booking.setBookingNumber(UUID.randomUUID().toString());
        booking.setPayment(payment);

        Booking savedBooking=bookingRepository.save(booking);

        selectedSeats.forEach(seat->
        {
            seat.setBooking(savedBooking);
            seat.setStatus("BOOKED");
        });
        showSeatRepository.saveAll(selectedSeats);
        return mapToBookingDto(savedBooking,selectedSeats);

    }

    public BookingDto getBookingById(Long id ){
        Booking booking=bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Booking Not Found with the id: "+id));

        List<ShowSeat>seats=showSeatRepository.findAll().stream()
                .filter(seat -> (
                    seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId())
                )).collect(Collectors.toList());
                return mapToBookingDto(booking,seats);
    }

    public BookingDto getBookingByNumber(String bookingNumber){
        Booking booking=bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(()->new ResourceNotFoundException("Booking Not Found with the id: "+bookingNumber));

        List<ShowSeat>seats=showSeatRepository.findAll().stream()
                .filter(seat -> (
                        seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId())
                )).collect(Collectors.toList());
        return mapToBookingDto(booking,seats);
    }

    public List<BookingDto> getBookingByUserId(Long userId){
       List<Booking> bookings=bookingRepository.findByUserId(userId);
       return bookings.stream()
               .map(booking->{
                   List<ShowSeat> seats=showSeatRepository.findAll()
                           .stream()
                           .filter(seat -> (
                                   seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId())
                           )).collect(Collectors.toList());
        return mapToBookingDto(booking,seats);
               }).collect(Collectors.toList());
    }

    @Transactional
    public BookingDto cancelBooking(Long id){
        Booking booking=bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with the id: "+id));

        booking.setStatus("CANCELLED");
        List<ShowSeat> seats=showSeatRepository.findAll()
                .stream()
                .filter(seat -> (
                        seat.getBooking()!=null && seat.getBooking().getId().equals(booking.getId())
                )).toList();

        seats.forEach(seat->{
            seat.setStatus("AVAILABLE");
            seat.setBooking(null);

        });
        if(booking.getPayment()!=null){
            booking.getPayment().setStatus("REFUNDED");
        }

        Booking updateBooking=bookingRepository.save(booking);
        showSeatRepository.saveAll(seats);

        return mapToBookingDto(updateBooking,seats);

    }

    public BookingDto mapToBookingDto(Booking booking,List<ShowSeat> seats){

        BookingDto bookingDto=modelMapper.map(booking,BookingDto.class);

        List<ShowSeatDto> seatDto=seats.stream()
                .map(seat->modelMapper.map(seat,ShowSeatDto.class))
                .toList();
        bookingDto.setSeat(seatDto);

        ShowDto showDto = bookingDto.getShow();

        if (showDto != null) {
            List<ShowSeatDto> availableSeats = showSeatRepository.findByShowId(showDto.getId())
                    .stream()
                    .filter(seat -> "AVAILABLE".equals(seat.getStatus()))
                    .map(seat -> modelMapper.map(seat, ShowSeatDto.class))
                    .collect(Collectors.toList());

            showDto.setAvailableSeat(availableSeats);
        }
        return bookingDto;
    }
}
