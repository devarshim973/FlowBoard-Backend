package com.flowboard.card_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "DTO for sending a single notification")
public class BulkNotificationRequestDto {
    @Schema(description = "Recipient user ID", example = "2")
    private List<Integer> recipientIds;

    @Schema(description = "Actor (triggering user) ID", example = "10")
    private Integer actorId;

    @Schema(description = "Notification title", example = "New Comment")
    private String title;

    @Schema(description = "Notification message", example = "Someone commented on your card")
    private String message;

    @Schema(description = "Related entity ID", example = "55")
    private Integer relatedId;

    @Schema(description = "Notification type", example = "COMMENT")
    private NotificationType notificationType;

    @Schema(description = "Related entity type", example = "CARD")
    private RelatedType relatedType;
}