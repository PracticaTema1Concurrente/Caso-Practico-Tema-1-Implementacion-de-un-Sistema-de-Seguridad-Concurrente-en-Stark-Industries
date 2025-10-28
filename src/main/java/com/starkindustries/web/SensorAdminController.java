package com.starkindustries.web;

import com.starkindustries.domain.SensorDevice;
import com.starkindustries.domain.SensorType;
import com.starkindustries.domain.repository.SensorDeviceRepo;
import com.starkindustries.service.SensorFleetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class SensorAdminController {

    private final SensorFleetService fleet;
    private final SensorDeviceRepo deviceRepo;

    public SensorAdminController(SensorFleetService fleet, SensorDeviceRepo deviceRepo) {
        this.fleet = fleet;
        this.deviceRepo = deviceRepo;
    }

    @PostMapping
    public SensorDevice create(@RequestBody SensorDevice d) {
        return fleet.register(d);
    }

    @GetMapping
    public List<SensorDevice> list() {
        return fleet.listAll();
    }

    @GetMapping("/byType/{type}")
    public List<SensorDevice> byType(@PathVariable SensorType type) {
        return deviceRepo.findByType(type);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<String> start(@PathVariable Long id) {
        fleet.start(id);
        return ResponseEntity.ok("Sensor " + id + " iniciado");
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<String> stop(@PathVariable Long id) {
        fleet.stop(id);
        return ResponseEntity.ok("Sensor " + id + " detenido");
    }

    @PostMapping("/{id}/period/{ms}")
    public ResponseEntity<String> changePeriod(@PathVariable Long id, @PathVariable long ms) {
        fleet.updatePeriod(id, ms);
        return ResponseEntity.ok("Periodo actualizado a " + ms + " ms");
    }

    @PostMapping("/startAll")
    public ResponseEntity<String> startAll() {
        fleet.startAllInactive();
        return ResponseEntity.ok("Arrancando todos los inactivos");
    }

    @PostMapping("/stopAll")
    public ResponseEntity<String> stopAll() {
        fleet.stopAll();
        return ResponseEntity.ok("Parando todos");
    }
}
