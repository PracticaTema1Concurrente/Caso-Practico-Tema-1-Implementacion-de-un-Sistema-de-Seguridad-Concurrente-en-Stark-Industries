package com.starkindustries.service;

import com.starkindustries.domain.AlertReview;
import com.starkindustries.domain.repository.AlertReviewRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class AlertReviewService {

    private final AlertReviewRepo repo;

    public AlertReviewService(AlertReviewRepo repo) {
        this.repo = repo;
    }

    /**
     * Guarda solo si decision == "DANGER".
     * Devuelve la entidad guardada o null si se ignoró (por ser SAFE).
     */
    @Transactional
    public AlertReview saveDangerOnly(AlertReview in) {
        if (in == null) return null;
        if (!"DANGER".equalsIgnoreCase(in.getDecision())) {
            return null; // no guardamos decisiones SAFE
        }
        in.setDecision("DANGER");
        return repo.save(in);
    }

    public List<AlertReview> findRecent(int limit) {
        // Mejor con Pageable si tu repo extiende JpaRepository:
        return repo.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }
}
