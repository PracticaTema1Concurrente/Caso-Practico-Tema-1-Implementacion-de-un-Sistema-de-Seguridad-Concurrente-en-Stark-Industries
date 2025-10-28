package com.starkindustries.web;

import com.starkindustries.domain.*;
import com.starkindustries.domain.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorReadingsController {

    private final SensorReadingRepo baseRepo;
    private final TempReadingRepo tempRepo;
    private final HumidityReadingRepo humRepo;
    private final MotionReadingRepo motionRepo;

    public SensorReadingsController(SensorReadingRepo baseRepo,
                                    TempReadingRepo tempRepo,
                                    HumidityReadingRepo humRepo,
                                    MotionReadingRepo motionRepo) {
        this.baseRepo = baseRepo;
        this.tempRepo = tempRepo;
        this.humRepo = humRepo;
        this.motionRepo = motionRepo;
    }

    // All readings (now with device eagerly loaded)
    @GetMapping("/readings")
    public List<SensorReading> all(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "100") int size) {
        return baseRepo
                .findAllWithDevice(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .getContent();
    }

    // By device id (with device)
    @GetMapping("/readings/byDevice/{deviceId}")
    public List<SensorReading> byDevice(@PathVariable Long deviceId,
                                        @RequestParam(defaultValue = "50") int limit) {
        return baseRepo.findByDeviceIdWithDevice(deviceId,
                PageRequest.of(0, limit, Sort.by("createdAt").descending()));
    }

    // Since timestamp (with device)
    @GetMapping("/readings/since")
    public List<SensorReading> since(@RequestParam
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                     OffsetDateTime since,
                                     @RequestParam(defaultValue = "200") int limit) {
        return baseRepo.findByCreatedAtAfterWithDevice(since,
                PageRequest.of(0, limit, Sort.by("createdAt").descending()));
    }

    // By type
    @GetMapping("/readings/temp")
    public List<TempReading> temps(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "100") int size) {
        return tempRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .getContent();
    }

    @GetMapping("/readings/hum")
    public List<HumidityReading> hums(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "100") int size) {
        return humRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .getContent();
    }

    @GetMapping("/readings/motion")
    public List<MotionReading> motions(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "100") int size) {
        return motionRepo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .getContent();
    }

}

