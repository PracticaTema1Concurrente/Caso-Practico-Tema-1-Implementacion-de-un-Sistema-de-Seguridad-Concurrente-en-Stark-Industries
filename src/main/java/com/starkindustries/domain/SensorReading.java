package com.starkindustries.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sensor_reading")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "sensor_type")
public abstract class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Un identificador lógico del sensor (ej. “T-1”, “H-2”)
    @Column(nullable = false, length = 50)
    private String sensorId;

    // Valor numérico común (en algunos tipos pondremos 0/1)
    @Column(nullable = false)
    private double value;

    @Column(length = 16)
    private String unit; // "°C", "%", "bool", etc.

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private SensorDevice device;

    // --- getters/setters ---
    public Long getId() { return id; }
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public SensorDevice getDevice() { return device; }
    public void setDevice(SensorDevice device) { this.device = device; }
}
