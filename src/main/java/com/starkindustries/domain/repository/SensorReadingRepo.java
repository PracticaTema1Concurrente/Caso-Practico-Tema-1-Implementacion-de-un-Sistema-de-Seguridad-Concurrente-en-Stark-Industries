package com.starkindustries.domain.repository;

import com.starkindustries.domain.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface SensorReadingRepo extends JpaRepository<SensorReading, Long> {
    List<SensorReading> findBySensorId(String sensorId);

    // ===== Métodos existentes (lazy) =====
    List<SensorReading> findByDevice_Id(Long deviceId, Pageable pageable);
    List<SensorReading> findByCreatedAtAfter(OffsetDateTime since, Pageable pageable);

    // ===== Nuevos: cargan device de forma EAGER mediante EntityGraph =====
    @EntityGraph(attributePaths = "device")
    @Query("select r from SensorReading r")
    Page<SensorReading> findAllWithDevice(Pageable pageable);

    @EntityGraph(attributePaths = "device")
    @Query("select r from SensorReading r where r.device.id = :deviceId")
    List<SensorReading> findByDeviceIdWithDevice(Long deviceId, Pageable pageable);

    @EntityGraph(attributePaths = "device")
    @Query("select r from SensorReading r where r.createdAt > :since")
    List<SensorReading> findByCreatedAtAfterWithDevice(OffsetDateTime since, Pageable pageable);
}