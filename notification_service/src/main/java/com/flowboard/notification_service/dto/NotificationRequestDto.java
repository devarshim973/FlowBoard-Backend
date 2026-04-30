package com.flowboard.notification_service.dto;

import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for sending bulk notifications")
public class NotificationRequestDto {
    @NotNull
    @Schema(description = "List of recipient user IDs", example = "[1,2,3]")
    private Integer recipientId;

    @NotNull
    @Schema(description = "Actor (triggering user) ID", example = "10")
    private Integer actorId;

    @NotNull
    @Schema(description = "Type of notification", example = "COMMENT")
    private NotificationType notificationType;

    @NotBlank
    @Schema(description = "Notification title", example = "Task Assigned")
    private String title;

    @NotBlank
    @Schema(description = "Notification message", example = "You have been assigned a new task")
    private String message;

    @NotNull
    @Schema(description = "Related entity ID", example = "101")
    private Integer relatedId;

    @NotNull
    @Schema(description = "Related entity type", example = "CARD")
    private RelatedType relatedType;
}
