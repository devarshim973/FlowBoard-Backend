package com.flowboard.notification_service.service.impl;

import com.flowboard.notification_service.entity.Notification;
import com.flowboard.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate;

    @Override
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
    public void sendAssignmentEmail(String to, Notification notification) {
        String subject = "You’ve been assigned a new task";

        String body = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h2 style="color: #2c3e50;">New Assignment</h2>
            <p>Hello,</p>
            <p>You have been assigned a new task.</p>

            <div style="background-color:#f4f6f7; padding:10px; border-radius:5px;">
                <strong>%s</strong>
                <p>%s</p>
            </div>

            <p>Please check your dashboard for more details.</p>

            <br>
            <p style="color:gray; font-size:12px;">FlowBoard Notification</p>
        </body>
        </html>
        """.formatted(notification.getTitle(), notification.getMessage());

        send(to, subject, body);
    }

    @Override
    public void sendMentionEmail(String to, Notification notification) {
        String subject = "You were mentioned in a comment";

        String body = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h2 style="color: #2980b9;">You were mentioned</h2>
            <p>Hello,</p>
            <p>You were mentioned in a comment.</p>

            <div style="background-color:#eef5fb; padding:10px; border-radius:5px;">
                <strong>%s</strong>
                <p>%s</p>
            </div>

            <p>Check the discussion to respond.</p>

            <br>
            <p style="color:gray; font-size:12px;">FlowBoard Notification</p>
        </body>
        </html>
        """.formatted(notification.getTitle(), notification.getMessage());

        send(to, subject, body);
    }

    @Override
    public void sendDueDateEmail(String to, Notification notification) {

        String subject = "Task deadline approaching";

        String body = """
        <html>
        <body style="font-family: Arial, sans-serif;">
            <h2 style="color: #e67e22;">Deadline Reminder</h2>
            <p>Hello,</p>
            <p>This is a reminder that your task deadline is approaching.</p>

            <div style="background-color:#fff4e6; padding:10px; border-radius:5px;">
                <strong>%s</strong>
                <p>%s</p>
            </div>

            <p>Please take necessary action before the deadline.</p>

            <br>
            <p style="color:gray; font-size:12px;">FlowBoard Notification</p>
        </body>
        </html>
        """.formatted(notification.getTitle(), notification.getMessage());

        send(to, subject, body);
    }
}
