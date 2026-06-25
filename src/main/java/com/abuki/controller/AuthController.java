package com.abuki.controller;

import com.abuki.dto.LoginRequest;
import com.abuki.model.Device;
import com.abuki.model.LoginHistory;
import com.abuki.model.User;
import com.abuki.repository.DeviceRepository;
import com.abuki.repository.LoginHistoryRepository;
import com.abuki.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private LoginHistoryRepository loginHistoryRepo;
    @Autowired private DeviceRepository deviceRepo;

    // POST /api/auth/login
    // Body: { "email": "admin@abuki.com", "password": "admin123", "deviceFingerprint": "..." }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            // ── Verify credentials FIRST ────────────────────────────────
            // We never want to reveal "this device is blocked" to someone
            // who doesn't even have the right password — that would leak
            // information to an attacker. Wrong credentials always fail
            // with the same generic message regardless of device status.
            String token = userService.login(req.getEmail(), req.getPassword());
            User user    = userService.getByEmail(req.getEmail());

            String fingerprint = req.getDeviceFingerprint();
            String ip           = extractClientIp(request);
            String userAgent    = request.getHeader("User-Agent");

            // ── Device lookup / registration / block check ──────────────
            // Only runs if the frontend actually sent a fingerprint. Older
            // clients / API callers without one simply skip device tracking
            // rather than being blocked outright.
            if (fingerprint != null && !fingerprint.isBlank()) {
                Device device = deviceRepo.findByUserIdAndFingerprint(user.getId(), fingerprint)
                    .orElseGet(() -> {
                        Device d = new Device();
                        d.setUser(user);
                        d.setUserEmail(user.getEmail());
                        d.setFingerprint(fingerprint);
                        return d;
                    });

                if (device.isBlocked()) {
                    // Credentials were correct, but this specific device has
                    // been disallowed by an admin. No token is issued.
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "This device has been blocked by an administrator. Contact your admin if this is unexpected."
                    ));
                }

                device.setLastUserAgent(userAgent);
                device.setLastIpAddress(ip);
                device.setLastSeenAt(LocalDateTime.now());
                deviceRepo.save(device);
            }

            // ── Record this login for the admin-only "Login History" page ──
            // Wrapped so a logging failure can never block a real login.
            try {
                LoginHistory entry = new LoginHistory();
                entry.setUser(user);
                entry.setUserEmail(user.getEmail());
                entry.setIpAddress(ip);
                entry.setUserAgent(userAgent);
                entry.setFingerprint(fingerprint);
                loginHistoryRepo.save(entry);
            } catch (Exception ignored) {
                // Never let history-logging break the login flow.
            }

            return ResponseEntity.ok(Map.of(
                "token",  token,
                "id",     user.getId(),
                "name",   user.getName(),
                "email",  user.getEmail(),
                "role",   user.getRole(),
                "status", user.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/auth/me  (requires token)
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal String email) {
        try {
            return ResponseEntity.ok(userService.getByEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Render is fronted by Cloudflare, so the real client IP arrives in
     * X-Forwarded-For (first entry in the chain) rather than the raw
     * remote address, which would just be Render's/Cloudflare's proxy IP.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}