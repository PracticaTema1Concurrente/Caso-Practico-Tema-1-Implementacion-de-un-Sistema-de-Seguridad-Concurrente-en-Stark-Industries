package com.starkindustries.domain.repository;

import com.starkindustries.domain.AlertReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertReviewRepo extends JpaRepository<AlertReview, Long> {
    // En el futuro puedes añadir queries (por tipo, fechas, etc.)
}
