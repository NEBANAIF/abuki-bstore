package com.abuki.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One row per successful login. Lets an ADMIN see which devices/IPs are
 * connecting to the app, and spot anything unfamiliar.
 *
 * We intentionally do NOT store a parsed "device name" — just the raw
 * User-Agent string and IP address. Parsing is done in the frontend for
 * display only, so the raw data is always available if needed later.
 */
@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Denormalized for convenience — survives even if the user is later deleted
    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "logged_in_at", updatable = false)
    private LocalDateTime loggedInAt;

    @PrePersist
    protected void onCreate() {
        if (loggedInAt == null) loggedInAt = LocalDateTime.now();
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getLoggedInAt() { return loggedInAt; }
    public void setLoggedInAt(LocalDateTime loggedInAt) { this.loggedInAt = loggedInAt; }
}
