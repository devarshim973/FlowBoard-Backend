package com.flowboard.card_service.dto;

import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardRequestDto {
    @Schema(description = "List ID where card belongs", example = "1")
    @NotNull(message = "List id cannot be null")
    private Integer listId;

    @Schema(description = "Board ID where card belongs", example = "2")
    @NotNull(message = "Board id cannot be null")
    private Integer boardId;

    @Schema(description = "Card title", example = "Implement login API")
    @NotNull(message = "Title cannot be null")
    private String title;

    @Schema(description = "Card description", example = "Create JWT based authentication")
    private String description;

    @Schema(description = "Priority level", example = "HIGH")
    private Priority priority;

    @Schema(description = "Card status", example = "TODO")
    private Status status;

    @Schema(description = "Due date and time")
    private LocalDateTime dueDate;

    @Schema(description = "Start date and time")
    private LocalDateTime startDate;

    /* Validate that this assignee have card modification request */
    @Schema(description = "Assigned user ID", example = "5")
    private Integer assigneeId;
}