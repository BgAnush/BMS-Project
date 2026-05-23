package com.bank.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.bank.auth.security.JwtService;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    // Use a long enough string for HS256 (32+ characters)
    private final String testSecret = "bank_auth_service_super_secure_secret_key_2026_bank_secure";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Since @Value won't work in a plain JUnit test, we inject it manually
        ReflectionTestUtils.setField(jwtService, "secret", testSecret);
    }

    @Test
    void testGenerateAndExtractToken() {
        // Arrange
        Integer profileId = 1;
        String customerId = "CUS001";
        String role = "USER";

        // Act
        String token = jwtService.generateToken(profileId, customerId, role);
        Claims claims = jwtService.extract(token);

        // Assert (SonarQube will be happy with these)
        assertNotNull(token);
        assertEquals(customerId, claims.getSubject());
        assertEquals(profileId, claims.get("profileId", Integer.class));
        assertEquals(role, claims.get("role", String.class));
        assertNotNull(claims.getExpiration());
    }
}