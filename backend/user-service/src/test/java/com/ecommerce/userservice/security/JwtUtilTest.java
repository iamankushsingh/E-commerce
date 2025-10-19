package com.ecommerce.userservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-super-secret-jwt-key-must-be-at-least-256-bits-long-for-security-purposes";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateToken_Success() {
        // Given
        String email = "test@example.com";
        Long userId = 1L;
        String role = "CUSTOMER";
        String firstName = "John";
        String lastName = "Doe";

        // When
        String token = jwtUtil.generateToken(email, userId, role, firstName, lastName);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void testExtractEmail_Success() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, 1L, "CUSTOMER", "John", "Doe");

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractUserId_Success() {
        // Given
        Long userId = 123L;
        String token = jwtUtil.generateToken("test@example.com", userId, "CUSTOMER", "John", "Doe");

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractRole_Success() {
        // Given
        String role = "ADMIN";
        String token = jwtUtil.generateToken("test@example.com", 1L, role, "John", "Doe");

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void testExtractExpiration_Success() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testIsTokenExpired_NotExpired() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        // Given - Create JWT with immediate expiration
        JwtUtil shortLivedJwt = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwt, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedJwt, "expiration", 1L); // 1 millisecond
        
        String token = shortLivedJwt.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");
        
        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, 1L, "CUSTOMER", "John", "Doe");

        // When
        Boolean isValid = jwtUtil.validateToken(token, email);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidEmail() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");

        // When
        Boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_WithoutEmail() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // Given
        String emptyToken = "";

        // When
        Boolean isValid = jwtUtil.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // When/Then
        assertThrows(NullPointerException.class, () -> {
            jwtUtil.validateToken(null);
        });
    }

    @Test
    void testExtractEmail_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When/Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractEmail(malformedToken);
        });
    }

    @Test
    void testExtractUserId_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When/Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUserId(malformedToken);
        });
    }

    @Test
    void testExtractRole_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When/Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractRole(malformedToken);
        });
    }

    @Test
    void testIsTokenExpired_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.here";

        // When
        Boolean isExpired = jwtUtil.isTokenExpired(malformedToken);

        // Then
        assertTrue(isExpired); // Should return true for invalid tokens
    }

    @Test
    void testGenerateToken_WithNullValues() {
        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> {
            String token = jwtUtil.generateToken(null, null, null, null, null);
            assertNotNull(token);
        });
    }

    @Test
    void testGenerateToken_WithSpecialCharacters() {
        // Given
        String email = "test+tag@example.com";
        String firstName = "John-Paul";
        String lastName = "O'Brien";

        // When
        String token = jwtUtil.generateToken(email, 1L, "CUSTOMER", firstName, lastName);

        // Then
        assertNotNull(token);
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void testTokenContainsCorrectClaims() {
        // Given
        String email = "test@example.com";
        Long userId = 999L;
        String role = "ADMIN";
        String firstName = "Jane";
        String lastName = "Smith";

        // When
        String token = jwtUtil.generateToken(email, userId, role, firstName, lastName);

        // Then
        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertEquals(role, jwtUtil.extractRole(token));
        assertNotNull(jwtUtil.extractExpiration(token));
    }

    @Test
    void testTokenExpirationTime() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");
        
        // When
        Date expiration = jwtUtil.extractExpiration(token);
        Date now = new Date();
        long difference = expiration.getTime() - now.getTime();

        // Then - Should be approximately TEST_EXPIRATION (with some tolerance)
        assertTrue(difference > 0);
        assertTrue(difference <= TEST_EXPIRATION + 1000); // 1 second tolerance
    }

    @Test
    void testMultipleTokensForSameUser() {
        // Given
        String email = "test@example.com";
        
        // When
        String token1 = jwtUtil.generateToken(email, 1L, "CUSTOMER", "John", "Doe");
        String token2 = jwtUtil.generateToken(email, 1L, "CUSTOMER", "John", "Doe");

        // Then - Tokens should be different (different issue time)
        assertNotEquals(token1, token2);
        
        // But both should be valid
        assertTrue(jwtUtil.validateToken(token1, email));
        assertTrue(jwtUtil.validateToken(token2, email));
    }

    @Test
    void testTokenValidation_DifferentSecret() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "CUSTOMER", "John", "Doe");
        
        // Create new JwtUtil with different secret
        JwtUtil differentSecretJwt = new JwtUtil();
        ReflectionTestUtils.setField(differentSecretJwt, "secret", "different-secret-key-that-is-also-256-bits-long-for-jwt-security");
        ReflectionTestUtils.setField(differentSecretJwt, "expiration", TEST_EXPIRATION);

        // When/Then - Should fail validation with different secret
        assertThrows(Exception.class, () -> {
            differentSecretJwt.extractEmail(token);
        });
    }
}

