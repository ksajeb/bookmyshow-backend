package com.bookmyshow.bms.controller;

import com.bookmyshow.bms.dto.TheaterDto;
import com.bookmyshow.bms.service.TheaterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/theater")
public class TheaterController {

    @Autowired
    private TheaterService theaterService;
    // CREATE THEATER
    @PostMapping
    public ResponseEntity<TheaterDto> createTheater(@RequestBody TheaterDto theaterDto) {
        TheaterDto created = theaterService.createTheater(theaterDto);
        return ResponseEntity.ok(created);
    }

    // GET THEATER BY ID
    @GetMapping("/{id}")
    public ResponseEntity<TheaterDto> getTheaterById(@PathVariable Long id) {
        TheaterDto theater = theaterService.getTheaterById(id);
        return ResponseEntity.ok(theater);
    }

    // GET ALL THEATERS
    @GetMapping
    public ResponseEntity<List<TheaterDto>> getAllTheaters() {
        List<TheaterDto> theaters = theaterService.getALlTheater();
        return ResponseEntity.ok(theaters);
    }

    // GET ALL THEATERS BY CITY
    @GetMapping("/city/{city}")
    public ResponseEntity<List<TheaterDto>> getAllTheatersByCity(@PathVariable String city) {
        List<TheaterDto> theaters = theaterService.getAllTheaterByCity(city);
        return ResponseEntity.ok(theaters);
    }

    // UPDATE THEATER
    @PutMapping("/{id}")
    public ResponseEntity<TheaterDto> updateTheater(
            @PathVariable Long id,
            @RequestBody TheaterDto theaterDto) {

        TheaterDto updated = theaterService.updateTheater(id, theaterDto);
        return ResponseEntity.ok(updated);
    }

    // DELETE THEATER
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.ok("Theater deleted successfully with ID: " + id);
    }
}
