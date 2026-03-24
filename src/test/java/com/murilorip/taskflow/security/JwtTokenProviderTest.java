package com.murilorip.taskflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-testing-purposes-only-do-not-use-in-production-123456";
        long expiration = 3600000; // 1 hour
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    @DisplayName("should generate a valid JWT token")
    void shouldGenerateValidToken() {
        Authentication authentication = createAuthentication("john@example.com");

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("should extract username from token")
    void shouldExtractUsernameFromToken() {
        String email = "john@example.com";
        Authentication authentication = createAuthentication(email);
        String token = jwtTokenProvider.generateToken(authentication);

        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(extractedUsername).isEqualTo(email);
    }

    @Test
    @DisplayName("should validate a correct token")
    void shouldValidateCorrectToken() {
        Authentication authentication = createAuthentication("john@example.com");
        String token = jwtTokenProvider.generateToken(authentication);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("should reject an invalid token")
    void shouldRejectInvalidToken() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("should reject a tampered token")
    void shouldRejectTamperedToken() {
        Authentication authentication = createAuthentication("john@example.com");
        String token = jwtTokenProvider.generateToken(authentication);
        String tamperedToken = token + "tampered";

        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        assertThat(isValid).isFalse();
    }

    private Authentication createAuthentication(String email) {
        UserDetails userDetails = new User(email, "password", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
