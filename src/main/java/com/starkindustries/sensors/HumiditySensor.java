package com.starkindustries.sensors;

import com.starkindustries.domain.HumidityReading;
import com.starkindustries.domain.SensorReading;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component("humiditySensor")
public class HumiditySensor implements SensorSimulator {
    @Override public String type() { return "HUM"; }
    @Override public SensorReading nextReading() {
        HumidityReading r = new HumidityReading();
        r.setSensorId("H-1");
        r.setValue(ThreadLocalRandom.current().nextDouble(35.0, 65.0)); // %
        return r;
    }
}
