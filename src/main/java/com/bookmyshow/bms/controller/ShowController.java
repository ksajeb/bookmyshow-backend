package com.bookmyshow.bms.controller;

import com.bookmyshow.bms.dto.ShowDto;
import com.bookmyshow.bms.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/show")
public class ShowController {

    @Autowired
    private ShowService showService;

    // CREATE SHOW
    @PostMapping
    public ResponseEntity<ShowDto> createShow(@RequestBody ShowDto showDto) {
        ShowDto createdShow = showService.createShow(showDto);
        return ResponseEntity.ok(createdShow);
    }

    // GET SHOW BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ShowDto> getShowById(@PathVariable Long id) {
        ShowDto show = showService.getShowById(id);
        return ResponseEntity.ok(show);
    }

    // GET ALL SHOWS
    @GetMapping
    public ResponseEntity<List<ShowDto>> getAllShows() {
        List<ShowDto> shows = showService.getAllShows();
        return ResponseEntity.ok(shows);
    }

    // GET SHOWS BY MOVIE ID
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowDto>> getShowsByMovie(@PathVariable Long movieId) {
        List<ShowDto> shows = showService.getShowsByMovie(movieId);
        return ResponseEntity.ok(shows);
    }

    // GET SHOWS BY MOVIE ID AND CITY
    @GetMapping("/movie/{movieId}/city/{city}")
    public ResponseEntity<List<ShowDto>> getShowsByMovieAndCity(
            @PathVariable Long movieId,
            @PathVariable String city) {

        List<ShowDto> shows = showService.getShowsByMovieAndCity(movieId, city);
        return ResponseEntity.ok(shows);
    }

    // GET SHOWS BY DATE RANGE
    @GetMapping("/date-range")
    public ResponseEntity<List<ShowDto>> getShowsByDateRange(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<ShowDto> shows = showService.getShowsByDateRange(startDate, endDate);
        return ResponseEntity.ok(shows);
    }
}
