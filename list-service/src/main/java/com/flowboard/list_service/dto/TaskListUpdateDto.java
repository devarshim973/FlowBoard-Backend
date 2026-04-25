package com.flowboard.list_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskListUpdateDto {
    @Schema(description = "Updated task list name", example = "In Progress")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 to 100 characters")
    private String name;

    @Schema(description = "Updated UI color hex code", example = "#10B981")
    @NotBlank(message = "String color is required")
    private String color;
}
