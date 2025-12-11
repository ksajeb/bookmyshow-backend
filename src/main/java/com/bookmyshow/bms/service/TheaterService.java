package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.TheaterDto;
import com.bookmyshow.bms.entity.Theater;
import com.bookmyshow.bms.exception.ResourceNotFoundException;
import com.bookmyshow.bms.repository.TheatreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TheaterService {

    @Autowired
    private TheatreRepository theatreRepository;

    @Autowired
    private ModelMapper modelMapper;

    public TheaterDto createTheater(TheaterDto theaterDto){
        Theater theater=modelMapper.map(theaterDto,Theater.class);
        Theater saved=theatreRepository.save(theater);
        return modelMapper.map(saved,TheaterDto.class);
    }

    public TheaterDto getTheaterById(Long id){
        Theater theater=theatreRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Theater not found with the id: "+id));
        return modelMapper.map(theater,TheaterDto.class);
    }

    public List<TheaterDto> getALlTheater(){
        return theatreRepository.findAll()
                .stream()
                .map(theater -> modelMapper.map(theater,TheaterDto.class))
                .toList();
    }

    public List<TheaterDto> getAllTheaterByCity(String city) {
        return theatreRepository.findByCityIgnoreCase(city)
                .stream()
                .map(theater -> modelMapper.map(theater, TheaterDto.class))
                .toList();
    }

    public void deleteTheater(Long id) {
        Theater theater = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));

        theatreRepository.delete(theater);
    }

    public TheaterDto updateTheater(Long id, TheaterDto theaterDto) {
        Theater existingTheater = theatreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));

        existingTheater.setName(theaterDto.getName());
        existingTheater.setAddress(theaterDto.getAddress());
        existingTheater.setCity(theaterDto.getCity());
        existingTheater.setTotalScreen(theaterDto.getTotalScreen());

        Theater updatedTheater = theatreRepository.save(existingTheater);
        return modelMapper.map(updatedTheater, TheaterDto.class);
    }

}
