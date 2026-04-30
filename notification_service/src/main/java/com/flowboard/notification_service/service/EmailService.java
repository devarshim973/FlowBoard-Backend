package com.flowboard.notification_service.service;

import com.flowboard.notification_service.entity.Notification;

public interface EmailService {
    public void send(String toEmail, String subject, String body);

    public void sendAssignmentEmail(String recipientEmail, Notification notification);

    public void sendMentionEmail(String recipientEmail, Notification notification);

    public void sendDueDateEmail(String recipientEmail, Notification notification);
}
