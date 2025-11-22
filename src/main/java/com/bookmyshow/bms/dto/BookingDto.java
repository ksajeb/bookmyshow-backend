package com.bookmyshow.bms.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BookingDto {
    private Long id;
    private String bookingNumber;
    private LocalDateTime bookingTime;
    private UserDto user;
    private String status;
    private Double totalAmount;
    private List<ShowSeatDto> seat;
    private PaymentDto payment;
    private ShowDto show;
}
