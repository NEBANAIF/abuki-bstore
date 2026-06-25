package com.abuki.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One row per (user, browser/device) pair, identified by a client-generated
 * fingerprint stored in that browser's localStorage.
 *
 * IMPORTANT — what this can and cannot do:
 *   - It recognizes "this is the same browser as last time I saw it" for a
 *     given user. That's the only thing a web app can actually observe.
 *   - It is NOT a hardware device ID. Clearing localStorage / using a
 *     different browser / private/incognito mode all produce a new
 *     fingerprint, which will show up here as a new, unnamed device.
 *   - Blocking a device blocks future LOGIN attempts that present its
 *     fingerprint. It does not revoke an already-issued JWT immediately
 *     (the existing token remains valid until it expires) — see
 *     AuthController for where the block is enforced.
 */
@Entity
@Table(name = "devices", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "fingerprint" })
})
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "user_email", nullable = false, length = 150)
    private String userEmail;

    @Column(nullable = false, length = 100)
    private String fingerprint;

    // Admin-editable friendly label, e.g. "Nebil's laptop". Null until set.
    @Column(name = "device_name", length = 100)
    private String deviceName;

    // Raw UA from the most recent login with this fingerprint — used by the
    // frontend to suggest a default label (e.g. "Chrome on Windows") until
    // the admin renames it.
    @Column(name = "last_user_agent", length = 500)
    private String lastUserAgent;

    @Column(name = "last_ip_address", length = 64)
    private String lastIpAddress;

    @Column(nullable = false)
    private boolean blocked = false;

    @Column(name = "first_seen_at", updatable = false)
    private LocalDateTime firstSeenAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (firstSeenAt == null) firstSeenAt = now;
        if (lastSeenAt == null) lastSeenAt = now;
    }

    // ── Getters & Setters ────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getLastUserAgent() { return lastUserAgent; }
    public void setLastUserAgent(String lastUserAgent) { this.lastUserAgent = lastUserAgent; }

    public String getLastIpAddress() { return lastIpAddress; }
    public void setLastIpAddress(String lastIpAddress) { this.lastIpAddress = lastIpAddress; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public LocalDateTime getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(LocalDateTime firstSeenAt) { this.firstSeenAt = firstSeenAt; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
