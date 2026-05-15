package ru.ssau.netequip.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService("TestSecretKeyForHmac256MustBe32CharsLong!");
    }

    @Test
    void testGenerateToken() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        payload.put("roles", List.of("ADMIN"));
        payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

        String token = tokenService.generateToken(payload);

        assertNotNull(token);
        assertTrue(token.contains("."));
        assertEquals(2, token.split("\\.").length);
    }

    @Test
    void testValidateValidToken() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        payload.put("roles", List.of("ADMIN"));
        payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

        String token = tokenService.generateToken(payload);
        Map<String, Object> result = tokenService.validateToken(token);

        assertNotNull(result);
        assertEquals(1, ((Number) result.get("userId")).intValue());
    }

    @Test
    void testValidateExpiredToken() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        payload.put("exp", System.currentTimeMillis() / 1000 - 3600); // час назад

        String token = tokenService.generateToken(payload);
        Map<String, Object> result = tokenService.validateToken(token);

        assertNull(result);
    }

    @Test
    void testValidateInvalidSignature() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

        String token = tokenService.generateToken(payload);
        String tamperedToken = token + "abc";
        Map<String, Object> result = tokenService.validateToken(tamperedToken);

        assertNull(result);
    }

    @Test
    void testValidateTokenWithoutExp() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 1L);
        // нет exp

        String token = tokenService.generateToken(payload);
        Map<String, Object> result = tokenService.validateToken(token);

        assertNull(result);
    }

    @Test
    void testValidateMalformedToken() {
        Map<String, Object> result = tokenService.validateToken("not-a-valid-token");
        assertNull(result);
    }

    @Test
    void testConstructorWithShortSecret() {
        assertThrows(IllegalStateException.class, () -> new TokenService("short"));
    }

    @Test
    void testTokenContainsPayloadData() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", 42L);
        payload.put("roles", List.of("USER"));
        payload.put("exp", System.currentTimeMillis() / 1000 + 3600);

        String token = tokenService.generateToken(payload);
        Map<String, Object> result = tokenService.validateToken(token);

        assertNotNull(result);
        assertEquals(42, ((Number) result.get("userId")).intValue());
        List<?> roles = (List<?>) result.get("roles");
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }
}