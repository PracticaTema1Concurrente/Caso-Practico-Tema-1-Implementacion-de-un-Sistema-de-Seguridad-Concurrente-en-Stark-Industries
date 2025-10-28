package com.starkindustries.service;

import com.starkindustries.domain.SensorReading;
import com.starkindustries.domain.repository.SensorReadingRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorIngestService {

    private static final Logger log = LoggerFactory.getLogger(SensorIngestService.class);
    private final SensorReadingRepo baseRepo;

    public SensorIngestService(SensorReadingRepo baseRepo) {
        this.baseRepo = baseRepo;
    }

    @Async("sensorExecutor")
    @Transactional
    public void ingestAsync(SensorReading reading) {
        baseRepo.save(reading);
        log.info("Ingested {} reading from {} -> {}{}",
                reading.getClass().getSimpleName(),
                reading.getSensorId(),
                reading.getValue(),
                reading.getUnit() == null ? "" : reading.getUnit());
    }
}
