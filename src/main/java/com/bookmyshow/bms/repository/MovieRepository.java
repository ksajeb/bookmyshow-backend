package com.bookmyshow.bms.repository;

import com.bookmyshow.bms.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByLanguageIgnoreCase(String language);

    List<Movie> findByGenreIgnoreCase(String genre);

    List<Movie> findByTitleContainingIgnoreCase(String title);

}