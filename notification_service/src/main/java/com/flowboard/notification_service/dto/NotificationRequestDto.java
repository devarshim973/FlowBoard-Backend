package com.flowboard.notification_service.dto;

import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
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
public class NotificationRequestDto {
    @NotNull
    private Integer recipientId;

    @NotNull
    private Integer actorId;

    @NotNull
    private NotificationType notificationType;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private Integer relatedId;

    @NotNull
    private RelatedType relatedType;
}
