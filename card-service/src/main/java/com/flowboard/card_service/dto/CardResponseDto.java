package com.flowboard.card_service.dto;

import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardResponseDto {
    private Integer cardId;

    private Integer listId;

    private Integer boardId;

    private String title;

    private String description;

    private Integer position;

    private Priority priority;

    private Status status;

    private LocalDateTime dueDate;

    private LocalDateTime startDate;

    private Integer assigneeId;

    private Integer createdById;

    private Boolean isArchived;

    private String coverColor;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}