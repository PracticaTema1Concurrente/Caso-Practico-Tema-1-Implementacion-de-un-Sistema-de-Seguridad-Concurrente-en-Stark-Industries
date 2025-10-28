package com.starkindustries.sensors;

import com.starkindustries.domain.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class TempGenerator implements SensorValueGenerator {
    @Override public boolean supports(String t) { return "TEMP".equals(t); }

    @Override public SensorReading generate(SensorDevice d) {
        TempReading r = new TempReading();
        r.setDevice(d);
        r.setSensorId(d.getSensorId());
        r.setValue(ThreadLocalRandom.current().nextDouble(18, 28));
        r.setUnit(d.getUnit() != null ? d.getUnit() : "°C");
        return r;
    }
}
