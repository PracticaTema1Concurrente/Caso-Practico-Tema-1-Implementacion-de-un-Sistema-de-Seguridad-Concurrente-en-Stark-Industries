package com.starkindustries.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "alert_reviews", indexes = {
        @Index(name = "idx_alert_reviews_sensor", columnList = "sensorId"),
        @Index(name = "idx_alert_reviews_type", columnList = "type"),
        @Index(name = "idx_alert_reviews_createdAt", columnList = "createdAt")
})
public class AlertReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la lectura original (si lo tienes; puede ser null) */
    private Long readingId;

    /** Ej. "T-1", "H-1", "M-1" */
    @Column(nullable = false)
    private String sensorId;

    /** TEMP | HUM | MOTION (en mayúsculas) */
    @Column(nullable = false, length = 16)
    private String type;

    /** Valor medido (double) */
    private Double value;

    /** Unidad, ej. "°C", "%", "bool" */
    private String unit;

    /** Notas opcionales introducidas al confirmar */
    @Column(length = 2000)
    private String notes;

    /** Usuario que confirmó la alerta */
    @Column(nullable = false)
    private String decidedBy;

    /** Siempre guardamos solo cuando es DANGER */
    @Column(nullable = false, length = 16)
    private String decision; // "DANGER"

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Getters & Setters
    public Long getId() { return id; }
    public Long getReadingId() { return readingId; }
    public void setReadingId(Long readingId) { this.readingId = readingId; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDecidedBy() { return decidedBy; }
    public void setDecidedBy(String decidedBy) { this.decidedBy = decidedBy; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public Instant getCreatedAt() { return createdAt; }
}
