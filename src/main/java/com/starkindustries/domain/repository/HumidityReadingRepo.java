package com.starkindustries.domain.repository;

import com.starkindustries.domain.HumidityReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HumidityReadingRepo extends JpaRepository<HumidityReading, Long> { }