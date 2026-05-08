package com.flowboard.list_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskListOrderRequestDto {
    @Schema(description = "Task list ID", example = "1")
    @NotNull(message = "Task List id cannot be null")
    private Integer taskListId;

    @Schema(description = "New position of task list", example = "2")
    @NotNull(message = "Task List position is required")
    private Integer position;
}
