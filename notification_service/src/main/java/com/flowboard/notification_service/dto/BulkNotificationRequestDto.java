package com.flowboard.notification_service.dto;

import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "DTO for sending a single notification")
public class BulkNotificationRequestDto {
    @Size(min = 1, message = "At least one recipient is required")
    @Schema(description = "Recipient user ID", example = "2")
    private List<Integer> recipientIds;

    @NotNull
    @Schema(description = "Actor (triggering user) ID", example = "10")
    private Integer actorId;

    @NotBlank
    @Schema(description = "Notification title", example = "New Comment")
    private String title;

    @NotBlank
    @Schema(description = "Notification message", example = "Someone commented on your card")
    private String message;

    @NotNull
    @Schema(description = "Related entity ID", example = "55")
    private Integer relatedId;

    @NotNull
    @Schema(description = "Notification type", example = "COMMENT")
    private NotificationType notificationType;

    @NotNull
    @Schema(description = "Related entity type", example = "CARD")
    private RelatedType relatedType;
}