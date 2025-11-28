package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.ShowDto;
import com.bookmyshow.bms.dto.ShowSeatDto;
import com.bookmyshow.bms.entity.Movie;
import com.bookmyshow.bms.entity.Screen;
import com.bookmyshow.bms.entity.Show;
import com.bookmyshow.bms.entity.ShowSeat;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.MovieRepository;
import com.bookmyshow.bms.repository.ScreenRepository;
import com.bookmyshow.bms.repository.ShowRepository;
import com.bookmyshow.bms.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;

    private final MovieRepository movieRepository;

    private final ScreenRepository screenRepository;

    private final ShowSeatRepository showSeatRepository;

    private final ModelMapper modelMapper;

    public ShowDto createShow(ShowDto showDto){

        Movie movie=movieRepository.findById(showDto.getMovie().getId())
                .orElseThrow(()->new ResourceNotFoundException("Movie not found."));

        Screen screen=screenRepository.findById(showDto.getScreen().getId())
                .orElseThrow(()->new ResourceNotFoundException("Screen not found."));

        Show show=modelMapper.map(showDto,Show.class);
        show.setMovie(movie);
        show.setScreen(screen);

        Show savedShow=showRepository.save(show);

        List<ShowSeat> availableSeats=showSeatRepository.findByShowIdAndStatus(savedShow.getId(),"AVAILABLE");

        List<ShowSeatDto> seatDtos = availableSeats.stream()
                .map(seat -> modelMapper.map(seat, ShowSeatDto.class))
                .toList();

        ShowDto response = modelMapper.map(savedShow, ShowDto.class);
        response.setAvailableSeat(seatDtos);

        return response;
    }
}
