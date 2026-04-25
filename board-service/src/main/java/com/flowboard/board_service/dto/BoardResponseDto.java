package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardResponseDto {
    @Schema(description = "Board ID", example = "1")
    private Integer boardId;

    @Schema(description = "Workspace ID", example = "1")
    private Integer workspaceId;

    @Schema(description = "Board name", example = "Development Board")
    private String name;

    @Schema(description = "Board description", example = "Tasks related to backend development")
    private String description;

    @Schema(description = "Board background", example = "blue-gradient")
    private String background;

    @Schema(description = "Board visibility", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "Created by user ID", example = "5")
    private Integer createdById;

    @Schema(description = "Board closed status", example = "false")
    private boolean isClosed;

    @Schema(description = "Created date and time")
    private LocalDateTime createdAt;

    @Schema(description = "Updated date and time")
    private LocalDateTime updatedAt;
}