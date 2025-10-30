package com.starkindustries.service;

import com.starkindustries.domain.SensorReading;
import com.starkindustries.domain.repository.SensorReadingRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

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

        if (!shouldIngest(reading)) {
            log.info("⏭️ Skipped {} reading from {} [{}] (variabilidad simulada)",
                    reading.getClass().getSimpleName(),
                    reading.getSensorId(),
                    reading.getUnit());
            return;
        }

        baseRepo.save(reading);
        log.info("✅ Ingested {} reading from {} -> {}{}",
                reading.getClass().getSimpleName(),
                reading.getSensorId(),
                reading.getValue(),
                reading.getUnit() == null ? "" : reading.getUnit());
    }

    /**
     * Decide si se debe guardar o no la lectura según la unidad (unit).
     * Usa probabilidades fijas hardcodeadas.
     */
    private boolean shouldIngest(SensorReading reading) {
        String unit = reading.getUnit() == null ? "" : reading.getUnit().trim().toLowerCase();
        double probability;

        switch (unit) {
            case "°c":      // temperatura
            case "c":
                probability = 0.8;
                break;
            case "%":       // humedad
                probability = 0.6;
                break;
            case "bool":    // movimiento, interruptores
                probability = reading.getValue() == 1.0 ? 0.2 : 0.9;
                break;
            default:
                probability = 1.0; // si no se reconoce la unidad, siempre guarda
        }

        return ThreadLocalRandom.current().nextDouble() < probability;
    }
}
