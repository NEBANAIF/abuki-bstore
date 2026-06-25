package com.abuki.controller;

import com.abuki.dto.LoginRequest;
import com.abuki.model.LoginHistory;
import com.abuki.model.User;
import com.abuki.repository.LoginHistoryRepository;
import com.abuki.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private LoginHistoryRepository loginHistoryRepo;

    // POST /api/auth/login
    // Body: { "email": "admin@abuki.com", "password": "admin123" }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        try {
            String token = userService.login(req.getEmail(), req.getPassword());
            User user    = userService.getByEmail(req.getEmail());

            // ── Record this login for the admin-only "Login History" page ──
            // Wrapped so a logging failure can never block a real login.
            try {
                LoginHistory entry = new LoginHistory();
                entry.setUser(user);
                entry.setUserEmail(user.getEmail());
                entry.setIpAddress(extractClientIp(request));
                entry.setUserAgent(request.getHeader("User-Agent"));
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