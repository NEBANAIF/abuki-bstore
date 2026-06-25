package com.abuki.dto;

public class LoginRequest {
    private String email;
    private String password;

    // Generated and persisted client-side (localStorage) — identifies the
    // browser/device making the request. Optional: a null/missing value
    // just means "no device tracking for this login" rather than an error.
    private String deviceFingerprint;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
}