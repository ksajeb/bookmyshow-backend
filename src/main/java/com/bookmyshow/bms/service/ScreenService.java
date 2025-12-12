package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.ScreenDto;
import com.bookmyshow.bms.entity.Screen;
import com.bookmyshow.bms.entity.Theater;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.ScreenRepository;
import com.bookmyshow.bms.repository.ShowRepository;
import com.bookmyshow.bms.repository.TheatreRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
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
        log.info("Creating screen with data: {}", screenDto);
        if (screenDto.getTheater() == null || screenDto.getTheater().getId() == null) {
            log.error("Failed to create screen - Theater ID missing");
            throw new IllegalArgumentException("Theater ID must be provided");
        }

        log.info("Fetching Theater with ID {}", screenDto.getTheater().getId());
        Theater theater = theaterRepository.findById(screenDto.getTheater().getId())
                .orElseThrow(() -> {
                    log.error("Theater not found with ID {}", screenDto.getTheater().getId());
                    return new ResourceNotFoundException("Theater not found");
                });

        Screen screen = modelMapper.map(screenDto, Screen.class);
        screen.setTheater(theater);

        log.info("Saving new Screen into database");
        Screen saved = screenRepository.save(screen);

        log.info("Screen created successfully with ID {}", saved.getId());
        return modelMapper.map(saved, ScreenDto.class);

    }

    public List<ScreenDto> getAllScreen(){
        log.info("Fetching all screens");
        List<Screen> screens=screenRepository.findAll();
        log.info("Retrieved {} screens", screens.size());
        return screens.stream()
                .map(screen->modelMapper.map(screen,ScreenDto.class))
                .toList();
    }


    public ScreenDto getScreenById(Long id) {
        log.info("Fetching screen by ID {}", id);
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Screen not found with ID: {}", id);
                   return new ResourceNotFoundException("Screen not found with the ID:"+id);
                });
        log.info("Screen found with ID {}", id);
        return modelMapper.map(screen, ScreenDto.class);
    }

    public List<ScreenDto> getScreensByTheaterId(Long theaterId) {
        log.info("Fetching screens for theater ID {}", theaterId);
        List<Screen> screens = screenRepository.findByTheaterId(theaterId);
        if(screens.isEmpty()){
            log.warn("No screen found with this theater ID: {}", theaterId);
            throw new ResourceNotFoundException("No screen found with this theater ID: " + theaterId);
        }
        log.info("Found {} screens for theater ID {}", screens.size(), theaterId);
        return screens.stream()
                .map(screen -> modelMapper.map(screen, ScreenDto.class))
                .toList();
    }

    public ScreenDto updateScreen(Long id, ScreenDto screenDto) {
        log.info("Updating screen with ID {}", id);
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Screen not found with ID: {}", id);
                   return new ResourceNotFoundException("Screen not found");
                });

        log.info("Updating fields for screen ID {}", id);
        screen.setName(screenDto.getName());
        screen.setTotalSeats(screenDto.getTotalSeats());

        Screen updated = screenRepository.save(screen);

        log.info("Screen updated successfully with ID {}", id);
        return modelMapper.map(updated, ScreenDto.class);
    }

    public void deleteScreen(Long id) {
        log.info("Deleting screen with ID {}", id);
        Screen screen = screenRepository.findById(id)
                .orElseThrow(() ->
                {
                    log.error("Screen not found with ID: {}", id);
                   return new ResourceNotFoundException("Screen not found with the id: " + id);
                });
        screenRepository.delete(screen);
        log.info("Screen deleted successfully with ID {}", id);
    }

}
