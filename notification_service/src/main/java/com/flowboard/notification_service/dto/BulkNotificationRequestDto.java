package com.flowboard.notification_service.dto;

import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequestDto {
    @Size(min = 1, message = "At least one recipient is required")
    private List<Integer> recipientIds;

    @NotNull
    private Integer actorId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private Integer relatedId;

    @NotNull
    private NotificationType notificationType;

    @NotNull
    private RelatedType relatedType;
}