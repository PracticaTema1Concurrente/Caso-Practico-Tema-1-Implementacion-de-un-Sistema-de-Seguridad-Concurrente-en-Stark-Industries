package com.starkindustries.web;

import com.starkindustries.domain.*;
import com.starkindustries.domain.repository.*;
import com.starkindustries.service.SensorCoordinatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors/ops")
public class SensorController {

    private final TempReadingRepo tempRepo;
    private final HumidityReadingRepo humRepo;
    private final MotionReadingRepo motionRepo;
    private final SensorCoordinatorService coordinator;

    public SensorController(TempReadingRepo tempRepo,
                            HumidityReadingRepo humRepo,
                            MotionReadingRepo motionRepo,
                            SensorCoordinatorService coordinator) {
        this.tempRepo = tempRepo;
        this.humRepo = humRepo;
        this.motionRepo = motionRepo;
        this.coordinator = coordinator;
    }

    // --- Insertar manualmente (POST) ---
    @PostMapping("/readings/temp")
    public TempReading addTemp(@RequestBody TempReading r) {
        return tempRepo.save(r);
    }

    @PostMapping("/readings/hum")
    public HumidityReading addHum(@RequestBody HumidityReading r) {
        return humRepo.save(r);
    }

    @PostMapping("/readings/motion")
    public MotionReading addMotion(@RequestBody MotionReading r) {
        return motionRepo.save(r);
    }

    // --- Disparar una ronda concurrente de lectura simulada ---
    @PostMapping("/simulate/poll-once")
    public ResponseEntity<String> pollOnce() {
        coordinator.pollAllOnceAsync();
        return ResponseEntity.accepted().body("Lecturas solicitadas (procesando en segundo plano).");
    }
}