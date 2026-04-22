package com.flowboard.comment_service.dto;

import com.flowboard.comment_service.entity.NotificationType;
import com.flowboard.comment_service.entity.RelatedType;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequestDto {
    private List<Integer> recipientIds;

    private Integer actorId;

    private String title;

    private String message;

    private Integer relatedId;

    private NotificationType notificationType;

    private RelatedType relatedType;
}