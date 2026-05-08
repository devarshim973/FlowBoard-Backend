package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BoardRequestDto {
    @NotNull(message = "Workspace id cannot be null")
    @Schema(description = "Workspace ID", example = "1")
    private Integer workspaceId;

    @Schema(description = "Board name", example = "Development Board")
    @NotBlank(message = "Board name cannot be blank")
    @Size(min = 2, max = 100, message = "Board name must be between 2 to 100 characters")
    private String name;

    @Schema(description = "Board description", example = "Tasks related to backend development")
    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Schema(description = "Board background image or color", example = "blue-gradient")
    private String background;

    @Schema(description = "Board visibility", example = "PUBLIC")
    @NotNull(message = "Visibility must be PUBLIC or PRIVATE")
    private Visibility visibility;
}