package com.starkindustries.domain.repository;

import com.starkindustries.domain.TempReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempReadingRepo extends JpaRepository<TempReading, Long> { }
