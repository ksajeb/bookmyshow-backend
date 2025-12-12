package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.MovieDto;
import com.bookmyshow.bms.entity.Movie;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ModelMapper modelMapper;

    public MovieDto createMovie(MovieDto movieDto){
        log.info("Creating a new movie:{}",movieDto.getTitle());
        Movie movie= modelMapper.map(movieDto,Movie.class);
        Movie saveMovie=movieRepository.save(movie);
        log.info("Movie created successfully with the ID: {}",saveMovie.getId());
        return modelMapper.map(saveMovie,MovieDto.class);
    }

    public MovieDto getMovieById(Long id){
        log.info("Fetching movie with the ID: {}",id);
        Movie movie=movieRepository.findById(id)
                .orElseThrow(()->{
                    log.error("Movie not found with the ID: {}",id);
                    return new ResourceNotFoundException("Movie not found with the id: "+id);
                });
        log.info("Movie retrieved from database: {}",movie.getTitle());
        return modelMapper.map(movie,MovieDto.class);
    }

    public List<MovieDto> getAllMovies(){
        log.info("Fetching all movies.....");
        List<Movie> movies=movieRepository.findAll();
        log.info("Total movies fetched: {}",movies.size());
        return movies.stream()
                .map(movie -> modelMapper.map(movie,MovieDto.class))
                .toList();
    }

    public List<MovieDto> getMovieByLanguage(String language){
        log.info("Fetching movies by language: {}", language);
        List<Movie> movies=movieRepository.findByLanguageIgnoreCase(language);
        if (movies.isEmpty()) {
            log.warn("No movies found for language: '{}'", language);
            throw new ResourceNotFoundException("No movies found for language: " + language);
        }
        log.info("Total movies found for language '{}': {}", language, movies.size());
        return movies.stream()
                .map(movie -> modelMapper.map(movie,MovieDto.class))
                .toList();
    }

    public List<MovieDto> getMovieByGenre(String genre){
        log.info("Fetching movies by genre: {}", genre);
        List<Movie> movies=movieRepository.findByGenreIgnoreCase(genre);
        if(movies.isEmpty()){
            log.warn("No movies found for genre: '{}'", genre);
            throw new ResourceNotFoundException("No movies found for genre: " + genre);
        }
        log.info("Total movies found for genre '{}': {}", genre, movies.size());
        return movies.stream()
                .map(movie -> modelMapper.map(movie,MovieDto.class))
                .toList();
    }

    public List<MovieDto> searchMovieByTitle(String title){
        log.info("Searching movies by title containing: {}", title);
        List<Movie> movies=movieRepository.findByTitleContainingIgnoreCase(title);
        if(movies.isEmpty()){
            log.warn("No movies found for title: '{}'", title);
            throw new ResourceNotFoundException("No movies found for title: " + title);
        }

        log.info("Movies found matching '{}': {}", title, movies.size());
        return movies.stream()
                .map(movie -> modelMapper.map(movie,MovieDto.class))
                .toList();
    }

    public MovieDto updateMovie(Long id,MovieDto movieDto){
        log.info("Updating movie with ID: {}", id);
        Movie movie=movieRepository.findById(id)
                .orElseThrow(()->{
                    log.info("Movie not found with ID: {}", id);
                       return new ResourceNotFoundException("Movie not found with the id: "+id);
                });
        log.info("Updating movie details for: {}", movie.getTitle());
        movie.setTitle(movieDto.getTitle());
        movie.setDescription(movieDto.getDescription());
        movie.setLanguage(movieDto.getLanguage());
        movie.setGenre(movieDto.getGenre());
        movie.setDurationMins(movieDto.getDurationMins());
        movie.setReleaseDate(movieDto.getReleaseDate());
        movie.setPosterUrl(movieDto.getPosterUrl());

       Movie updatedMovie= movieRepository.save(movie);
        log.info("Movie updated successfully: {}", updatedMovie.getTitle());
       return modelMapper.map(updatedMovie,MovieDto.class);
    }

    public void deleteMovie(Long id){
        log.info("Deleting movie with ID: {}", id);
        Movie movie=movieRepository.findById(id)
                .orElseThrow(()-> {
                    log.error("Movie not found with ID: {}", id);
                    return new ResourceNotFoundException("Movie not found with the id: " + id);
                });
        movieRepository.delete(movie);
        log.info("Movie deleted successfully: {}", movie.getTitle());
    }
}
