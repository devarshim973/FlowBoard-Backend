package com.flowboard.auth_service.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    public String generateToken(String username);

    public String extractUserName(String token);

    boolean validateToken(String token, UserDetails userDetails);
}
