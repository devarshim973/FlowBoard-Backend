package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${app.mail.sender-name:FlowBoard}")
    private String senderName;

    private final JavaMailSender mailSender;

    public void send(String toEmail, String subject, String htmlContent) {
        if (senderEmail == null || senderEmail.isBlank()) {
            log.warn("SMTP is not configured yet. Skipping email to {}", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("SMTP mail sent successfully to {}", toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("SMTP mail preparation failed for {}: {}", toEmail, e.getMessage(), e);
        } catch (Exception e) {
            log.error("SMTP mail sending failed for {}: {}", toEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp){

        String htmlContent = """        
        <html>
          <body style="font-family: Arial, sans-serif; color: #333;">
            <h2>Password Reset OTP</h2>
            <p>Hello,</p>
            <p>Use the following OTP to reset your FlowBoard account password:</p>
            <h3 style="color: #007bff;">%s</h3>
            <p>This OTP will expire in 5 minutes.</p>
            <hr>
            <p style="font-size:12px;color:gray;">
              If you did not request this, you can safely ignore this email.
            </p>
          </body>
        </html>
        """.formatted(otp);

        this.send(toEmail, "FlowBoard - Password Reset OTP", htmlContent);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String verificationLink) {
        String subject = "FlowBoard - Verify Your Email Address";

        String htmlContent = """
    <html>
      <body style="font-family: Arial, sans-serif; color: #333;">
        <h2>Email Verification</h2>
        <p>Hello,</p>
        <p>Thank you for signing up with <b>FlowBoard</b>.</p>
        <p>Please click the button below to verify your email address:</p>
        <p>
          <a href="%s" 
             style="display:inline-block; padding:10px 20px; color:white; background-color:#007bff;
                    text-decoration:none; border-radius:5px;">
             Verify Email
          </a>
        </p>
        <p>If the button above does not work, copy and paste the following link into your browser:</p>
        <p style="color:#007bff;">%s</p>
        <hr>
        <p style="font-size:12px;color:gray;">
          If you did not create this account, you can safely ignore this email.
        </p>
      </body>
    </html>
    """.formatted(verificationLink, verificationLink);

        this.send(toEmail, subject, htmlContent);
    }
}
