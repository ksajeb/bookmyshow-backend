package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.MovieDto;
import com.bookmyshow.bms.entity.Movie;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ModelMapper modelMapper;

    public MovieDto createMovie(MovieDto movieDto){
        Movie movie= modelMapper.map(movieDto,Movie.class);
        Movie saveMovie=movieRepository.save(movie);
        return modelMapper.map(saveMovie,MovieDto.class);
    }

    public MovieDto getMovieById(Long id){
        Movie movie=movieRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Movie not found with the id: "+id));
        return modelMapper.map(movie,MovieDto.class);
    }

    public List<MovieDto> getAllMovies(){
        List<Movie> movies=movieRepository.findAll();
        return movies.stream()
                .map(movie -> modelMapper.map(movie,MovieDto.class))
                .toList();
    }

    public List<MovieDto> getMovieByLanguage(String language){
        List<Movie> movies=movieRepository.findByLanguage(language);
        return movies.stream()
                .map(movie -> modelMapper.map(movies,MovieDto.class))
                .toList();
    }

    public List<MovieDto> getMovieByGenre(String genre){
        List<Movie> movies=movieRepository.findByGenre(genre);
        return movies.stream()
                .map(movie -> modelMapper.map(movies,MovieDto.class))
                .toList();
    }

    public List<MovieDto> searchMovieByTitle(String title){
        List<Movie> movies=movieRepository.findByTitleContaining(title);
        return movies.stream()
                .map(movie -> modelMapper.map(movies,MovieDto.class))
                .toList();
    }

    public MovieDto updateMovie(Long id,MovieDto movieDto){
        Movie movie=movieRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Movie not found with the id: "+id));
        movie.setTitle(movieDto.getTitle());
        movie.setDescription(movieDto.getDescription());
        movie.setLanguage(movieDto.getLanguage());
        movie.setGenre(movieDto.getGenre());
        movie.setDurationMins(movieDto.getDurationMins());
        movie.setReleaseDate(movieDto.getReleaseDate());
        movie.setPosterUrl(movieDto.getPosterUrl());

       Movie updatedMovie= movieRepository.save(movie);
       return modelMapper.map(updatedMovie,MovieDto.class);
    }

    public void deleteMovie(Long id){
        Movie movie=movieRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Movie not found with the id: "+id));
        movieRepository.delete(movie);
    }
}
