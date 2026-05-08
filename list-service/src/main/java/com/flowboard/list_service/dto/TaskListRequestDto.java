package com.flowboard.list_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskListRequestDto {
    @Schema(description = "Board ID", example = "1")
    @NotNull(message = "User id is required")
    private Integer boardId;

    @Schema(description = "Task list name", example = "To Do")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 to 100 characters")
    private String name;


    @Schema(description = "UI color hex code", example = "#3B82F6")
    @NotBlank(message = "String color is required")
    private String color;
}
