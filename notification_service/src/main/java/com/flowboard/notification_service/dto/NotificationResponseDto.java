package com.flowboard.notification_service.dto;

import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponseDto {
    private Integer notificationId;

    private Integer recipientId;

    private Integer actorId;

    private NotificationType notificationType;

    private String title;

    private String message;

    private Integer relatedId;

    private RelatedType relatedType;

    private Boolean isRead = false;

    private LocalDateTime createdAt;
}
