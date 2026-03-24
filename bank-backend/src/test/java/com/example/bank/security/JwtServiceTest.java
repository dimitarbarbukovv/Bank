package com.example.bank.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    @Test
    void generateAndExtractUsername() {
        JwtService jwtService = new JwtService("very-long-demo-secret-key-change-me-123456");
        String token = jwtService.generateToken("admin", "ROLE_ADMIN");

        assertNotNull(token);
        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals("ROLE_ADMIN", jwtService.extractClaim(token, claims -> claims.get("role", String.class)));
    }
}
