package com.starkindustries.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("HUM")
public class HumidityReading extends SensorReading {
    public HumidityReading() { setUnit("%"); }
}
