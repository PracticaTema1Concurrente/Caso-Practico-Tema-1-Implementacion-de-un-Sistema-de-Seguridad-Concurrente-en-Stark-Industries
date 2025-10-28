package com.starkindustries.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Sirve para ocultar el device en la salida, puramente cosmético
//@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Table(name = "sensor_device")
public class SensorDevice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=50)
    private String sensorId;         // p.ej. "T-1", "H-2"

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private SensorType type;

    @Column(length=16)
    private String unit;             // "°C", "%", "bool"

    @Column(nullable=false)
    private boolean active = false;  // si está actualmente muestreando

    @Column(nullable=false)
    private long periodMs = 5_000;   // periodo de muestreo

    // getters/setters
    public Long getId() { return id; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public SensorType getType() { return type; }
    public void setType(SensorType type) { this.type = type; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getPeriodMs() { return periodMs; }
    public void setPeriodMs(long periodMs) { this.periodMs = periodMs; }
}

