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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("Creating booking for userId={} and showId={} with seats={}",
                bookingRequest.getUserId(), bookingRequest.getShowId(), bookingRequest.getSeatIds());

        User user = userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> {
                    log.error("User not found with id={}", bookingRequest.getUserId());
                    return new ResourceNotFoundException("User Not Found");
                });

        Show show = showRepository.findById(bookingRequest.getShowId())
                .orElseThrow(() -> {
                    log.error("Show not found with id={}", bookingRequest.getShowId());
                    return new ResourceNotFoundException("Show Not Found");
                });

        List<ShowSeat> selectedSeats = showSeatRepository.findAllById(bookingRequest.getSeatIds());
        if (selectedSeats.size() != bookingRequest.getSeatIds().size()) {
            log.error("One or more selected seats are invalid. Requested={} Found={}",
                    bookingRequest.getSeatIds().size(), selectedSeats.size());
            throw new ResourceNotFoundException("One or more selected seats are invalid.");
        }

        for (ShowSeat seat : selectedSeats) {
            if (!"AVAILABLE".equals(seat.getStatus())) {
                log.warn("Seat {} is not available", seat.getSeat().getSeatNumber());
                throw new SeatUnavailableException("Seat " + seat.getSeat().getSeatNumber() + " is not available");
            }
            seat.setStatus("LOCKED");
        }
        showSeatRepository.saveAll(selectedSeats);
        log.info("Seats locked: {}", bookingRequest.getSeatIds());

        Double totalAmount = selectedSeats.stream()
                .mapToDouble(ShowSeat::getPrice)
                .sum();
        log.info("Total amount calculated: {}", totalAmount);

        Payment payment = new Payment();
        payment.setAmount(totalAmount);
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentMethod(bookingRequest.getPaymentMethod());
        payment.setStatus("SUCCESS");
        payment.setTransactionId(UUID.randomUUID().toString());
        log.info("Payment created with transactionId={}", payment.getTransactionId());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShow(show);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus("CONFIRM");
        booking.setTotalAmount(totalAmount);
        booking.setBookingNumber(UUID.randomUUID().toString());
        booking.setPayment(payment);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking saved with bookingNumber={}", savedBooking.getBookingNumber());

        selectedSeats.forEach(seat -> {
            seat.setBooking(savedBooking);
            seat.setStatus("BOOKED");
        });
        showSeatRepository.saveAll(selectedSeats);
        log.info("Seats booked successfully: {}", bookingRequest.getSeatIds());

        return mapToBookingDto(savedBooking, selectedSeats);
    }

    public List<BookingDto> getAllBookings() {
        log.info("Fetching all bookings...");
        List<Booking> bookings = bookingRepository.findAll();
        log.info("Total bookings found: {}", bookings.size());
        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDto.class))
                .toList();
    }

    public BookingDto getBookingById(Long id) {
        log.info("Fetching booking by id={}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found with id={}", id);
                    return new ResourceNotFoundException("Booking Not Found with the id: " + id);
                });

        List<ShowSeat> seats = showSeatRepository.findAll().stream()
                .filter(seat -> seat.getBooking() != null && seat.getBooking().getId().equals(booking.getId()))
                .collect(Collectors.toList());
        log.info("Seats found for bookingId={}: {}", id, seats.size());

        return mapToBookingDto(booking, seats);
    }

    public BookingDto getBookingByNumber(String bookingNumber) {
        log.info("Fetching booking by bookingNumber={}", bookingNumber);
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> {
                    log.error("Booking not found with bookingNumber={}", bookingNumber);
                    return new ResourceNotFoundException("Booking Not Found with the id: " + bookingNumber);
                });

        List<ShowSeat> seats = showSeatRepository.findAll().stream()
                .filter(seat -> seat.getBooking() != null && seat.getBooking().getId().equals(booking.getId()))
                .collect(Collectors.toList());
        log.info("Seats found for bookingNumber={}: {}", bookingNumber, seats.size());

        return mapToBookingDto(booking, seats);
    }

    public List<BookingDto> getBookingByUserId(Long userId) {
        log.info("Fetching bookings by userId={}", userId);
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        log.info("Total bookings found for userId={}: {}", userId, bookings.size());

        return bookings.stream()
                .map(booking -> {
                    List<ShowSeat> seats = showSeatRepository.findAll().stream()
                            .filter(seat -> seat.getBooking() != null && seat.getBooking().getId().equals(booking.getId()))
                            .collect(Collectors.toList());
                    log.debug("Seats found for bookingId={}: {}", booking.getId(), seats.size());
                    return mapToBookingDto(booking, seats);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto cancelBooking(Long id) {
        log.info("Cancelling booking with id={}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Booking not found with id={}", id);
                    return new ResourceNotFoundException("Booking not found with the id: " + id);
                });

        booking.setStatus("CANCELLED");
        List<ShowSeat> seats = showSeatRepository.findAll().stream()
                .filter(seat -> seat.getBooking() != null && seat.getBooking().getId().equals(booking.getId()))
                .toList();

        seats.forEach(seat -> {
            seat.setStatus("AVAILABLE");
            seat.setBooking(null);
        });
        if (booking.getPayment() != null) {
            booking.getPayment().setStatus("REFUNDED");
            log.info("Payment refunded for bookingId={}", booking.getId());
        }

        Booking updatedBooking = bookingRepository.save(booking);
        showSeatRepository.saveAll(seats);
        log.info("Booking cancelled successfully for id={}", id);

        return mapToBookingDto(updatedBooking, seats);
    }

    public BookingDto mapToBookingDto(Booking booking, List<ShowSeat> seats) {
        log.debug("Mapping Booking to BookingDto for bookingId={}", booking.getId());
        BookingDto bookingDto = modelMapper.map(booking, BookingDto.class);

        List<ShowSeatDto> seatDto = seats.stream()
                .map(seat -> modelMapper.map(seat, ShowSeatDto.class))
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
            log.debug("Available seats mapped for showId={}: {}", showDto.getId(), availableSeats.size());
        }

        return bookingDto;
    }
}
