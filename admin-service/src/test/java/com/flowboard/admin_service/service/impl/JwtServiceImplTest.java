package com.flowboard.admin_service.service.impl;

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
    void generateTokenShouldIncludeExpectedClaims() {
        JwtServiceImpl jwtService = new JwtServiceImpl();
        String secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);

        String token = jwtService.generateToken("admin@flowboard.com", "ADMIN", 77);
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

        assertNotNull(token);
        assertEquals("admin@flowboard.com", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
        assertEquals(77, claims.get("userId", Integer.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }
}
