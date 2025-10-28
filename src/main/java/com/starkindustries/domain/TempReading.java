package com.starkindustries.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TEMP")
public class TempReading extends SensorReading {
    public TempReading() { setUnit("°C"); }
}
