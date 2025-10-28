package com.starkindustries.service;

import com.starkindustries.domain.SensorDevice;
import com.starkindustries.domain.SensorReading;
import com.starkindustries.domain.repository.SensorDeviceRepo;
import com.starkindustries.sensors.SensorValueGenerator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class SensorFleetService {

    private final SensorDeviceRepo deviceRepo;
    private final List<SensorValueGenerator> generators;
    private final SensorIngestService ingestService;
    private final ThreadPoolTaskScheduler scheduler;

    // deviceId -> tarea programada
    private final Map<Long, ScheduledFuture<?>> running = new ConcurrentHashMap<>();

    public SensorFleetService(SensorDeviceRepo deviceRepo,
                              List<SensorValueGenerator> generators,
                              SensorIngestService ingestService,
                              ThreadPoolTaskScheduler scheduler) {
        this.deviceRepo = deviceRepo;
        this.generators = generators;
        this.ingestService = ingestService;
        this.scheduler = scheduler;
    }

    private SensorValueGenerator pickGenerator(SensorDevice d) {
        return generators.stream()
                .filter(g -> g.supports(d.getType().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No generator for " + d.getType()));
    }

    @Transactional
    public SensorDevice register(SensorDevice d) {
        if (d.getUnit() == null || d.getUnit().isBlank()) {
            switch (d.getType()) {
                case TEMP -> d.setUnit("°C");
                case HUM -> d.setUnit("%");
                case MOTION -> d.setUnit("bool");
            }
        }
        return deviceRepo.save(d);
    }

    public List<SensorDevice> listAll() { return deviceRepo.findAll(); }

    @Transactional
    public void start(Long deviceId) {
        SensorDevice d = deviceRepo.findById(deviceId).orElseThrow();
        if (running.containsKey(d.getId())) return; // ya está corriendo

        var gen = pickGenerator(d);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            SensorReading r = gen.generate(d);
            ingestService.ingestAsync(r); // @Async: la escritura va por pool aparte
        }, d.getPeriodMs());

        running.put(d.getId(), future);
        d.setActive(true);
        deviceRepo.save(d);
    }

    @Transactional
    public void stop(Long deviceId) {
        Optional.ofNullable(running.remove(deviceId)).ifPresent(f -> f.cancel(false));
        deviceRepo.findById(deviceId).ifPresent(d -> {
            d.setActive(false);
            deviceRepo.save(d);
        });
    }

    @Transactional
    public void updatePeriod(Long deviceId, long newPeriodMs) {
        SensorDevice d = deviceRepo.findById(deviceId).orElseThrow();
        d.setPeriodMs(newPeriodMs);
        deviceRepo.save(d);
        if (d.isActive()) { // reprogramar si estaba activo
            stop(deviceId);
            start(deviceId);
        }
    }

    public void startAllInactive() {
        deviceRepo.findByActive(false).forEach(d -> {
            try { start(d.getId()); } catch (Exception ignored) {}
        });
    }

    public void stopAll() {
        new ArrayList<>(running.keySet()).forEach(this::stop);
    }
}
