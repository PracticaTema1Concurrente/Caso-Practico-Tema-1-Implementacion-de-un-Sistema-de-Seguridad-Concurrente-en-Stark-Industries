package com.starkindustries.domain.repository;

import com.starkindustries.domain.SensorDevice;
import com.starkindustries.domain.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SensorDeviceRepo extends JpaRepository<SensorDevice, Long> {
    Optional<SensorDevice> findBySensorId(String sensorId);
    List<SensorDevice> findByType(SensorType type);
    List<SensorDevice> findByActive(boolean active);
}
