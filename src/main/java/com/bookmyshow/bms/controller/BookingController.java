package com.bookmyshow.bms.controller;

import com.bookmyshow.bms.dto.BookingDto;
import com.bookmyshow.bms.dto.BookingRequestDto;
import com.bookmyshow.bms.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping()
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody BookingRequestDto bookingRequest){
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id){
        return  ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // GET BOOKING BY BOOKING NUMBER
    @GetMapping("/number/{bookingNumber}")
    public ResponseEntity<BookingDto> getBookingByNumber(@PathVariable String bookingNumber){
        return ResponseEntity.ok(bookingService.getBookingByNumber(bookingNumber));
    }

    // GET ALL BOOKINGS OF A USER BY USER ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDto>> getBookingByUserId(@PathVariable Long userId){
        return ResponseEntity.ok(bookingService.getBookingByUserId(userId));
    }

    // CANCEL BOOKING
    @PutMapping("/cancel/{id}")
    public ResponseEntity<BookingDto> cancelBooking(@PathVariable Long id){
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

}
