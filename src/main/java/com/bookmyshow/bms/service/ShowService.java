package com.bookmyshow.bms.service;

import com.bookmyshow.bms.dto.*;
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
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;

    private final MovieRepository movieRepository;

    private final ScreenRepository screenRepository;

    private final ShowSeatRepository showSeatRepository;

    private final ModelMapper modelMapper;

    public ShowDto createShow(ShowDto showDto){
        log.info("Creating new show for movieId={} screenId={}",
                showDto.getMovie().getId(), showDto.getScreen().getId());

        Movie movie=movieRepository.findById(showDto.getMovie().getId())
                .orElseThrow(()->
                {
                    log.error("Movie not found with ID: {}", showDto.getMovie().getId());
                   return new ResourceNotFoundException("Movie not found.");
                });

        Screen screen=screenRepository.findById(showDto.getScreen().getId())
                .orElseThrow(()->
                {
                    log.error("Screen not found with ID: {}", showDto.getScreen().getId());
                   return new ResourceNotFoundException("Screen not found.");
                });

        log.debug("Mapping showDto to Show entity");
        Show show=modelMapper.map(showDto,Show.class);
        show.setMovie(movie);
        show.setScreen(screen);

        Show savedShow=showRepository.save(show);

        log.info("Show created successfully with id:{}", savedShow.getId());

        log.debug("Creating show seats for Screen(id:{}) with {} seats",
                screen.getId(), screen.getSeats().size());

        List<ShowSeat> showSeats = screen.getSeats().stream().map(seat -> {
            ShowSeat showSeat = new ShowSeat();
            showSeat.setShow(savedShow);
            showSeat.setSeat(seat);
            showSeat.setStatus("AVAILABLE");
            showSeat.setPrice(seat.getBasePrice());
            return showSeat;
        }).toList();

        List<ShowSeat> availableSeats=showSeatRepository.saveAll(showSeats);
        log.info("Show seats created: {}", showSeats.size());

        List<ShowSeatDto> seatDtos = showSeats
                .stream()
                .map(seat -> modelMapper.map(seat, ShowSeatDto.class))
                .toList();

        ShowDto response = modelMapper.map(savedShow, ShowDto.class);
        response.setAvailableSeat(seatDtos);

        return response;
    }

    public ShowDto getShowById(Long id)
    {
        log.info("Fetching show by ID: {}", id);
        Show show=showRepository.findById(id)
                .orElseThrow(()->
                {
                    log.error("Show not found with ID: {}", id);
                   return new ResourceNotFoundException("Show not found  with id: " + id);
                });
        List<ShowSeat> availableSeats=
                showSeatRepository.findByShowIdAndStatus(show.getId(),"AVAILABLE");
        log.debug("Found {} available seats for show {}", availableSeats.size(), id);
        return mapToDto(show,availableSeats);
    }

    public List<ShowDto> getAllShows()
    {
        log.info("Fetching all shows...");
        List<Show> shows=showRepository.findAll();
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> availableSeats = showSeatRepository.findByShowIdAndStatus(show.getId(), "AVAILABLE");
                    return mapToDto(show,availableSeats);
                })
                .toList();
    }

    public List<ShowDto> getShowsByMovie(Long movieId)
    {
        log.info("Fetching shows for movieId={}", movieId);
        List<Show> shows=showRepository.findByMovieId(movieId);
        if (!movieRepository.existsById(movieId)) {
            log.error("Movie not found with ID: {}", movieId);
            throw new ResourceNotFoundException("Movie not found with id: " + movieId);
        }
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> availableSeats = showSeatRepository.findByShowIdAndStatus(show.getId(), "AVAILABLE");
                    return mapToDto(show,availableSeats);
                })
                .toList();
    }

    public List<ShowDto> getShowsByMovieAndCity(Long movieId,String city)
    {
        log.info("Fetching shows for movieId={}, city={}", movieId, city);
        List<Show> shows=showRepository.findByMovie_IdAndScreen_Theater_CityIgnoreCase(movieId,city);
        if (shows.isEmpty()) {
            log.warn("No shows found for movieId={} in city={}", movieId, city);
            throw new ResourceNotFoundException(
                    "No shows found for movieId: " + movieId + " in city: " + city
            );
        }
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> availableSeats = showSeatRepository.findByShowIdAndStatus(show.getId(), "AVAILABLE");
                    return mapToDto(show,availableSeats);
                })
                .toList();
    }

    public List<ShowDto> getShowsByDateRange(LocalDateTime startDate, LocalDateTime endDate)
    {
        log.info("Fetching shows between {} and {}", startDate, endDate);
        List<Show> shows=showRepository.findByStartTimeBetween(startDate,endDate);
        return shows.stream()
                .map(show -> {
                    List<ShowSeat> availableSeats = showSeatRepository.findByShowIdAndStatus(show.getId(), "AVAILABLE");
                    return mapToDto(show,availableSeats);
                })
                .toList();
    }

    private ShowDto mapToDto(Show show,List<ShowSeat> availableSeats)
    {
        log.debug("Mapping Show(id={}) to ShowDto", show.getId());
        ShowDto showDto= new ShowDto();
        showDto.setId(show.getId());
        showDto.setStartTime(show.getStartTime());
        showDto.setEndTime(show.getEndTime());

        showDto.setMovie(new MovieDto(
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                show.getMovie().getDescription(),
                show.getMovie().getLanguage(),
                show.getMovie().getGenre(),
                show.getMovie().getDurationMins(),
                show.getMovie().getReleaseDate(),
                show.getMovie().getPosterUrl()
        ));

        TheaterDto theaterDto=new TheaterDto(
                show.getScreen().getTheater().getId(),
                show.getScreen().getTheater().getName(),
                show.getScreen().getTheater().getAddress(),
                show.getScreen().getTheater().getCity(),
                show.getScreen().getTheater().getTotalScreen()
        );

        showDto.setScreen(new ScreenDto(
                show.getScreen().getId(),
                show.getScreen().getName(),
                show.getScreen().getTotalSeats(),
                theaterDto
        ));

        List<ShowSeatDto> seatDtos= availableSeats.stream()
                .map(seat->{
                    ShowSeatDto seatDto=new ShowSeatDto();
                    seatDto.setId(seat.getId());
                    seatDto.setStatus(seat.getStatus());
                    seatDto.setPrice(seat.getPrice());

                    SeatDto baseSeatDto=new SeatDto();
                    baseSeatDto.setId(seat.getSeat().getId());
                    baseSeatDto.setSeatNumber(seat.getSeat().getSeatNumber());
                    baseSeatDto.setSeatType(seat.getSeat().getSeatType());
                    baseSeatDto.setBasePrice(seat.getSeat().getBasePrice());
                    seatDto.setSeat(baseSeatDto);
                    return seatDto;
                })
                .collect(Collectors.toList());

        showDto.setAvailableSeat(seatDtos);
        log.debug("ShowDto mapping complete for showId={}, availableSeats={}",
                show.getId(), availableSeats.size());
        return showDto;
    }
}
