package com.bookmyshow.bms.repository;

import com.bookmyshow.bms.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theater, Long> {
    List<Theater> findByCityIgnoreCase(String city);
}