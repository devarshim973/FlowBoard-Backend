package com.flowboard.card_service.dto;

import com.flowboard.card_service.entity.ActivityType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class CardActivityResponseDto {
    private Integer activityId;

    private Integer cardId;

    private Integer actorId;

    private ActivityType activityType;

    private String details;

    private LocalDateTime createdAt;
}
