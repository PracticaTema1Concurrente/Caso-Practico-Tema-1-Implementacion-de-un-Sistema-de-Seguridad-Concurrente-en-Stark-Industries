package com.starkindustries.service;

import com.starkindustries.domain.SensorReading;
import com.starkindustries.sensors.SensorSimulator;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class SensorCoordinatorService {

    // Spring inyecta todos los beans SensorSimulator en un Map por nombre
    private final Map<String, SensorSimulator> simulators;
    private final SensorIngestService ingestService;

    public SensorCoordinatorService(Map<String, SensorSimulator> simulators,
                                    SensorIngestService ingestService) {
        this.simulators = simulators;
        this.ingestService = ingestService;
    }

    public Collection<SensorSimulator> allSimulators() {
        return simulators.values();
    }

    public void pollAllOnceAsync() {
        simulators.values().forEach(sim -> {
            SensorReading r = sim.nextReading();
            ingestService.ingestAsync(r); // @Async fan-out
        });
    }
}
