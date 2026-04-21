package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate;

    public void send(String toEmail, String subject, String htmlContent) {

        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> body = new HashMap<>();

        body.put("sender", Map.of(
                "email", senderEmail,
                "name", senderName
        ));

        body.put("to", List.of(
                Map.of("email", toEmail)
        ));

        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            log.info("Brevo Status: {}", response.getStatusCode());
            log.info("Brevo Body: {}", response.getBody());

        } catch (Exception e) {
            log.error("Brevo Error: {}", e.getMessage());
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
