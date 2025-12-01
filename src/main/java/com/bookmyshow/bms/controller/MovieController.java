package com.bookmyshow.bms.controller;

import com.bookmyshow.bms.dto.BookingDto;
import com.bookmyshow.bms.dto.BookingRequestDto;
import com.bookmyshow.bms.dto.MovieDto;
import com.bookmyshow.bms.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieDto> createMovie(@Valid @RequestBody MovieDto movieDto){
        return new ResponseEntity<>(movieService.createMovie(movieDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Long id){
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping
    public ResponseEntity<List<MovieDto>> getAllMovies(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<List<MovieDto>> getMovieByLanguage(@PathVariable String language){
        return ResponseEntity.ok(movieService.getMovieByLanguage(language));
    }

    // GET BY GENRE
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieDto>> getMovieByGenre(@PathVariable String genre){
        return ResponseEntity.ok(movieService.getMovieByGenre(genre));
    }

    // SEARCH MOVIE BY TITLE
    @GetMapping("/search")
    public ResponseEntity<List<MovieDto>> searchMovie(@RequestParam String title){
        return ResponseEntity.ok(movieService.searchMovieByTitle(title));
    }

    // UPDATE MOVIE
    @PutMapping("/{id}")
    public ResponseEntity<MovieDto> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDto movieDto){
        return ResponseEntity.ok(movieService.updateMovie(id, movieDto));
    }

    // DELETE MOVIE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id){
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Movie deleted successfully");
    }
}
