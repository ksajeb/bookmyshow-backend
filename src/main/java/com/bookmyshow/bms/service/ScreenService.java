package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.ScreenDto;
import com.bookmyshow.bms.entity.Screen;
import com.bookmyshow.bms.entity.Theater;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.ScreenRepository;
import com.bookmyshow.bms.repository.ShowRepository;
import com.bookmyshow.bms.repository.TheatreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScreenService {
    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private TheatreRepository theaterRepository;

    @Autowired
    private ModelMapper modelMapper;

    public ScreenDto createScreen(ScreenDto screenDto) {
        if (screenDto.getTheater() == null || screenDto.getTheater().getId() == null) {
            throw new IllegalArgumentException("Theater ID must be provided");
        }

        Theater theater = theaterRepository.findById(screenDto.getTheater().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found"));

        Screen screen = modelMapper.map(screenDto, Screen.class);
        screen.setTheater(theater);

        Screen saved = screenRepository.save(screen);

        return modelMapper.map(saved, ScreenDto.class);

    }

    public List<ScreenDto> getAllScreen(){
        List<Screen> screens=screenRepository.findAll();
        return screens.stream()
                .map(screen->modelMapper.map(screen,ScreenDto.class))
                .toList();
    }


    public ScreenDto getScreenById(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));
        return modelMapper.map(screen, ScreenDto.class);
    }

    public List<ScreenDto> getScreensByTheaterId(Long theaterId) {
        List<Screen> screens = screenRepository.findByTheaterId(theaterId);
        return screens.stream()
                .map(screen -> modelMapper.map(screen, ScreenDto.class))
                .toList();
    }

    public ScreenDto updateScreen(Long id, ScreenDto screenDto) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found"));

        screen.setName(screenDto.getName());
        screen.setTotalSeats(screenDto.getTotalSeats());

        Screen updated = screenRepository.save(screen);

        return modelMapper.map(updated, ScreenDto.class);
    }

    public void deleteScreen(Long id) {
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screen not found with the id: "+id));
        screenRepository.delete(screen);
    }

}
