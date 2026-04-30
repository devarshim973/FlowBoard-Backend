package com.flowboard.list_service.dto;

import jakarta.persistence.Column;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskListResponseDto {
    private Integer listId;

    private String boardId;

    private String name;

    private Integer position;

    private String color;

    private boolean isArchived = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}