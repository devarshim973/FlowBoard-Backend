package com.flowboard.admin_service.service;

public interface JwtService {
    String generateToken(String username, String role, Integer userId);
}
