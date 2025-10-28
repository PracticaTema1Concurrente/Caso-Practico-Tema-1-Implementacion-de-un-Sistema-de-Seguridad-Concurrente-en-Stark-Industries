package com.starkindustries.sensors;

import com.starkindustries.domain.*;
import org.springframework.stereotype.Component;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class HumidityGenerator implements SensorValueGenerator {
    @Override public boolean supports(String t) { return "HUM".equals(t); }

    @Override public SensorReading generate(SensorDevice d) {
        HumidityReading r = new HumidityReading();
        r.setDevice(d);
        r.setSensorId(d.getSensorId());
        r.setValue(ThreadLocalRandom.current().nextDouble(35, 65));
        r.setUnit(d.getUnit() != null ? d.getUnit() : "%");
        return r;
    }
}
