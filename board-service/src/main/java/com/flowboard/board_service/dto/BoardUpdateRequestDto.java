package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BoardUpdateRequestDto {
    @Schema(description = "Workspace ID", example = "1")
    @NotNull(message = "Workspace id cannot be null")
    private Integer workspaceId;

    @Schema(description = "Updated board name", example = "Updated Dev Board")
    @NotBlank(message = "Board name cannot be blank")
    @Size(min = 2, max = 100, message = "Board name must be between 2 to 100 characters")
    private String name;

    @Schema(description = "Updated description", example = "Updated board description")
    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Schema(description = "Updated background", example = "dark-theme")
    private String background;

    @Schema(description = "Board visibility", example = "PRIVATE")
    @NotNull(message = "Visibility must be PUBLIC or PRIVATE")
    private Visibility visibility;
}
