package com.flowboard.auth_service.service;

public interface JwtService {
    public String generateToken(String username, String role, Integer userId);
}
