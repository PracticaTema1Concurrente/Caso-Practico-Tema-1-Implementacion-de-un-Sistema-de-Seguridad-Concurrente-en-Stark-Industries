package com.starkindustries.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MOTION")
public class MotionReading extends SensorReading {
    public MotionReading() { setUnit("bool"); } // 0.0 = no, 1.0 = sí
}
