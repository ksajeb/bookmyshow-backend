package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.TheaterDto;
import com.bookmyshow.bms.entity.Theater;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.TheatreRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TheaterService {

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TheaterDto createTheater(TheaterDto theaterDto){
        log.info("Creating theater: {}", theaterDto.getName());
        Theater theater=modelMapper.map(theaterDto,Theater.class);
        Theater saved=theatreRepository.save(theater);
        log.info("Theater created successfully with ID: {}", saved.getId());
        return modelMapper.map(saved,TheaterDto.class);
    }

    public TheaterDto getTheaterById(Long id){
        log.info("Fetching theater with ID: {}", id);
        Theater theater=theatreRepository.findById(id)
                .orElseThrow(()->{
                    log.warn("Theater not found with ID: {}", id);
                    return new ResourceNotFoundException("Theater not found with the id: " + id);
                });
        log.info("Theater found: {}", theater.getName());
        return modelMapper.map(theater,TheaterDto.class);
    }

    public List<TheaterDto> getAllTheater(){
        log.info("Fetching all theaters...");
        List<Theater> theaters = theatreRepository.findAll();
        log.info("Total theaters found: {}", theaters.size());
        return theatreRepository.findAll()
                .stream()
                .map(theater -> modelMapper.map(theater,TheaterDto.class))
                .toList();
    }

    public List<TheaterDto> getAllTheaterByCity(String city) {
        log.info("Fetching theaters in city: {}", city);
        List<Theater> theaters= theatreRepository.findByCityIgnoreCase(city);
        if(theaters.isEmpty()){
            log.warn("No theaters found in city: {}", city);
            throw new ResourceNotFoundException("No theaters found in city: " + city);
        }
        log.info("Found {} theaters in city: {}", theaters.size(), city);
        return theaters.stream()
                .map(theater -> modelMapper.map(theater, TheaterDto.class))
                .toList();
    }

    public void deleteTheater(Long id) {
        log.info("Deleting theater with ID: {}", id);
        Theater theater = theatreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Theater not found with ID: {}", id);
                    return new ResourceNotFoundException("Theater not found with id: " + id);
                });
        log.info("Theater deleted successfully: {}", theater.getName());
        theatreRepository.delete(theater);
    }

    public TheaterDto updateTheater(Long id, TheaterDto theaterDto) {
        log.info("Updating theater with ID: {}", id);
        Theater existingTheater = theatreRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Theater not found with ID: {}", id);
                    return new ResourceNotFoundException("Theater not found with id: " + id);
                });

        existingTheater.setName(theaterDto.getName());
        existingTheater.setAddress(theaterDto.getAddress());
        existingTheater.setCity(theaterDto.getCity());
        existingTheater.setTotalScreen(theaterDto.getTotalScreen());

        Theater updatedTheater = theatreRepository.save(existingTheater);
        log.info("Theater updated successfully: {}", updatedTheater.getName());
        return modelMapper.map(updatedTheater, TheaterDto.class);
    }

}
