package com.flowboard.notification_service.service.impl;

import com.flowboard.notification_service.entity.Notification;
import com.flowboard.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Value("${spring.mail.username:}")
    private String senderEmail;

    @Value("${app.mail.sender-name:FlowBoard}")
    private String senderName;

    private final JavaMailSender mailSender;

    @Override
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
