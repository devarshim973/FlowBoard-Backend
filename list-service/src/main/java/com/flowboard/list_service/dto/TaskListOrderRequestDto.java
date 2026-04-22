package com.flowboard.list_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskListOrderRequestDto {
    @NotNull(message = "Task List id cannot be null")
    private Integer taskListId;

    @NotNull(message = "Task List position is required")
    private Integer position;
}
