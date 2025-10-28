package com.starkindustries.sensors;

import com.starkindustries.domain.MotionReading;
import com.starkindustries.domain.SensorReading;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component("motionSensor")
public class MotionSensor implements SensorSimulator {
    @Override public String type() { return "MOTION"; }
    @Override public SensorReading nextReading() {
        MotionReading r = new MotionReading();
        r.setSensorId("M-1");
        r.setValue(ThreadLocalRandom.current().nextBoolean() ? 1.0 : 0.0);
        return r;
    }
}
