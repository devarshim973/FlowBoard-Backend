package com.flowboard.auth_service.service;

public interface EmailService {
    public void sendOtpEmail(String toEmail, String otp);
    public void sendVerificationEmail(String toEmail, String verificationLink);

    void sendVerificationEmailForAdmin(String email, String s);

    void sendAccountActivationMail(String email);

    void sendAccountDeactivatedMail(String email);
}
