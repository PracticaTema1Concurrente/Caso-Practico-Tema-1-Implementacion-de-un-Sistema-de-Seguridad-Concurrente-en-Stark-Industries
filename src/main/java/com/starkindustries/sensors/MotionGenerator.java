package com.starkindustries.sensors;

import com.starkindustries.domain.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MotionGenerator implements SensorValueGenerator {
    @Override public boolean supports(String t) { return "MOTION".equals(t); }

    @Override public SensorReading generate(SensorDevice d) {
        MotionReading r = new MotionReading();
        r.setDevice(d);
        r.setSensorId(d.getSensorId());
        r.setValue(ThreadLocalRandom.current().nextBoolean() ? 1.0 : 0.0);
        r.setUnit(d.getUnit() != null ? d.getUnit() : "bool");
        return r;
    }
}
