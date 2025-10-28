package com.starkindustries.sensors;

import com.starkindustries.domain.SensorDevice;
import com.starkindustries.domain.SensorReading;

public interface SensorValueGenerator {
    boolean supports(String type);          // "TEMP", "HUM", "MOTION"
    SensorReading generate(SensorDevice d); // produce lectura
}