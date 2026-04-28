package com.pedro.finance_control;

import com.pedro.finance_control.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-finance-control-tests-2026");
        ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);
    }

    @Test
    void shouldGenerateAndParseTokenSuccessfully() {
        String token = jwtService.generateToken("pedro@email.com");

        assertNotNull(token);
        assertEquals("pedro@email.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "pedro@email.com"));
    }

    @Test
    void shouldReturnFalseWhenTokenDoesNotMatchEmail() {
        String token = jwtService.generateToken("pedro@email.com");

        assertFalse(jwtService.isTokenValid(token, "other@email.com"));
    }
}

