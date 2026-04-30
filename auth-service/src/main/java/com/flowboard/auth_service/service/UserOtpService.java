package com.flowboard.auth_service.service;


public interface UserOtpService {
    String sendOtp(String email);

    String sendSignupOtp(String email);

    void validateSignupOtp(String email, String otp);
}
