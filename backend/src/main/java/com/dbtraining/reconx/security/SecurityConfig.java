package com.dbtraining.reconx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ============================================================================
 * SecurityConfig
 * ============================================================================
 *
 * Fix:
 * - Provides PasswordEncoder bean required by AuthController.
 *
 * No JWT/RBAC ticket changes are included here.
 * ============================================================================
 */
@Configuration
public class SecurityConfig {

    /**
     * Password encoder used by AuthController for password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Existing Day-1 permissive security configuration.
     * Keep unchanged for now.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .permitAll()
                )
                .build();
    }
}
