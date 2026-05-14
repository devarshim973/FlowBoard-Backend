package com.flowboard.auth_service;

import com.flowboard.auth_service.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceImplTest {

    @Test
    void generateToken_shouldIncludeExpectedClaims() {
        JwtServiceImpl jwtService = new JwtServiceImpl();
        String secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);

        String token = jwtService.generateToken("user@flowboard.com", "USER", 99);
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertNotNull(token);
        assertEquals("user@flowboard.com", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
        assertEquals(99, claims.get("userId", Integer.class));
    }
}
