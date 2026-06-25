package com.abuki.repository;

import com.abuki.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByUserIdAndFingerprint(Long userId, String fingerprint);

    @Query("SELECT d FROM Device d ORDER BY d.lastSeenAt DESC")
    List<Device> findAllOrderedByMostRecent();

    List<Device> findByUserEmailOrderByLastSeenAtDesc(String userEmail);
}
