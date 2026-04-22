package com.flowboard.card_service.dto;

import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardUpdateDto {

    @Schema(description = "Updated title", example = "Implement secure login API")
    private String title;

    @Schema(description = "Updated description", example = "Use JWT and refresh token")
    private String description;

    @Schema(description = "Updated priority", example = "MEDIUM")
    private Priority priority;

    @Schema(description = "Updated status", example = "IN_PROGRESS")
    private Status status;

    @Schema(description = "Updated due date and time")
    private LocalDateTime dueDate;

    @Schema(description = "Updated start date and time")
    private LocalDateTime startDate;
}
