package com.abuki.controller;

import com.abuki.model.Device;
import com.abuki.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ADMIN-only — manage the registry of devices that have logged in.
 *
 *   GET  /api/devices            → list all, most recently active first
 *   PUT  /api/devices/{id}       → rename and/or block/unblock a device
 *
 * There is no DELETE: removing a device row would just let it silently
 * re-register as "new" on the next login, which defeats the point of an
 * audit trail. Blocking is the correct way to revoke a device.
 */
@RestController
@RequestMapping("/api/devices")
@PreAuthorize("hasRole('ADMIN')")
public class DeviceController {

    @Autowired private DeviceRepository deviceRepo;

    @GetMapping
    public ResponseEntity<List<Device>> getAll() {
        return ResponseEntity.ok(deviceRepo.findAllOrderedByMostRecent());
    }

    // Body: { "deviceName": "Nebil's laptop", "blocked": false }
    // Both fields optional — only the fields present are updated.
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return deviceRepo.findById(id).map(device -> {
            if (body.containsKey("deviceName")) {
                Object name = body.get("deviceName");
                device.setDeviceName(name == null ? null : name.toString());
            }
            if (body.containsKey("blocked")) {
                device.setBlocked(Boolean.TRUE.equals(body.get("blocked")));
            }
            deviceRepo.save(device);
            return ResponseEntity.ok(device);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
