package com.starkindustries.web;

import com.starkindustries.domain.AlertReview;
import com.starkindustries.service.AlertReviewService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertReviewController {

    private final AlertReviewService service;

    public AlertReviewController(AlertReviewService service) {
        this.service = service;
    }

    /** DTO simple para recibir la decisión desde revision.js */
    public record DecisionDTO(
            Long readingId,
            @NotBlank String sensorId,
            @NotBlank String type,   // "TEMP" | "HUM" | "MOTION"
            Double value,
            String unit,
            @NotBlank String decision, // "SAFE" | "DANGER"
            String notes
    ) {}

    @PostMapping("/decision")
    public ResponseEntity<?> registerDecision(@RequestBody DecisionDTO dto, Authentication auth) {
        if (dto == null || dto.decision() == null) {
            return ResponseEntity.badRequest().body("Payload inválido");
        }

        // Solo persistimos las peligrosas
        if (!"DANGER".equalsIgnoreCase(dto.decision())) {
            // No persistir SAFE; 204 sin cuerpo
            return ResponseEntity.noContent().build();
        }

        AlertReview ar = new AlertReview();
        ar.setReadingId(dto.readingId());
        ar.setSensorId(dto.sensorId());
        ar.setType(dto.type() != null ? dto.type().toUpperCase() : null);
        ar.setValue(dto.value());
        ar.setUnit(dto.unit());
        ar.setNotes(dto.notes());
        ar.setDecision("DANGER");

        // Usuario autenticado que dispara la confirmación
        String decidedBy = (auth != null) ? auth.getName() : "unknown";
        ar.setDecidedBy(decidedBy);

        AlertReview saved = service.saveDangerOnly(ar);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/recent")
    public ResponseEntity<?> recent(@RequestParam(defaultValue = "50") int size) {
        int limit = Math.max(1, Math.min(size, 200));
        // Como el repo es simple, tiramos de findAll y ordenamos en memoria.
        // Si prefieres, crea un método en el repo con Pageable y sort by createdAt desc.
        var list = service.findRecent(limit);
        return ResponseEntity.ok(list);
    }

}
