package com.abuki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Excludes UserDetailsServiceAutoConfiguration: this app uses a fully custom
 * JWT-based authentication flow (see JwtAuthFilter + AuthController), with
 * no Spring-managed UserDetailsService or AuthenticationManager. Without this
 * exclusion, Spring Boot falls back to generating a random in-memory "user"
 * account with a logged password on every boot — harmless since nothing in
 * this app's SecurityFilterChain uses basic/form auth, but it clutters logs
 * and signals incomplete security config. Excluding it removes the noise.
 */
@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class AbukiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AbukiApplication.class, args);
    }
}