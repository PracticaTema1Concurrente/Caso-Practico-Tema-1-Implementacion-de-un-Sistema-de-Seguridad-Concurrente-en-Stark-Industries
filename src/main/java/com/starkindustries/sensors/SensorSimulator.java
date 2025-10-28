package com.starkindustries.sensors;

import com.starkindustries.domain.SensorReading;

public interface SensorSimulator {
    String type();                     // "TEMP" | "HUM" | "MOTION"
    SensorReading nextReading();       // genera u obtiene la siguiente lectura
}
