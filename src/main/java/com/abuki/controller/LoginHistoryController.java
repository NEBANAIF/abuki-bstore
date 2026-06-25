package com.abuki.controller;

import com.abuki.model.LoginHistory;
import com.abuki.repository.LoginHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ADMIN-only — lets you see which devices/IPs have logged into the app,
 * most recent first. Read-only; no create/update/delete endpoints, since
 * this is an audit trail, not editable data.
 */
@RestController
@RequestMapping("/api/login-history")
@PreAuthorize("hasRole('ADMIN')")
public class LoginHistoryController {

    @Autowired private LoginHistoryRepository loginHistoryRepo;

    @GetMapping
    public ResponseEntity<List<LoginHistory>> getAll() {
        return ResponseEntity.ok(loginHistoryRepo.findAllOrderedByMostRecent());
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<LoginHistory>> getByUser(@PathVariable String email) {
        return ResponseEntity.ok(loginHistoryRepo.findByUserEmailOrderByLoggedInAtDesc(email));
    }
}
