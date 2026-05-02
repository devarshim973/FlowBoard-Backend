package com.flowboard.auth_service.service;


public interface UserOtpService {
    void sendOtp(String email);

    void sendSignupOtp(String email);

    void validateSignupOtp(String email, String otp);
}
