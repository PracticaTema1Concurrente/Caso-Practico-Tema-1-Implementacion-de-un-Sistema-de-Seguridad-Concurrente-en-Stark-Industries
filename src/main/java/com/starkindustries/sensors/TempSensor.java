package com.starkindustries.sensors;

import com.starkindustries.domain.TempReading;
import com.starkindustries.domain.SensorReading;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component("tempSensor")
@Scope("singleton") // por defecto; cámbialo a prototype si quieres instancias efímeras
public class TempSensor implements SensorSimulator {
    @Override public String type() { return "TEMP"; }
    @Override public SensorReading nextReading() {
        TempReading r = new TempReading();
        r.setSensorId("T-1");
        r.setValue(ThreadLocalRandom.current().nextDouble(18.0, 28.0)); // 18–28 °C
        return r;
    }
}