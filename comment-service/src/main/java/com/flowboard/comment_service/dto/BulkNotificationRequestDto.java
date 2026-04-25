package com.flowboard.comment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequestDto {
    @Schema(description = "Recipient user IDs", example = "[1,2,3]")
    private List<Integer> recipientIds;

    @Schema(description = "Actor user ID", example = "5")
    private Integer actorId;

    @Schema(description = "Notification title", example = "Card Assigned")
    private String title;

    @Schema(description = "Notification message", example = "You have been assigned a card")
    private String message;

    @Schema(description = "Related entity ID", example = "10")
    private Integer relatedId;

    @Schema(description = "Notification type", example = "ASSIGNMENT")
    private NotificationType notificationType;

    @Schema(description = "Related entity type", example = "CARD")
    private RelatedType relatedType;
}