package com.flowboard.workspace_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkspaceMemberRequestDto {
    @Schema(description = "Workspace ID", example = "1")
    @NotNull
    private Integer workspaceId;

    @Schema(description = "User ID to add as member", example = "5")
    @NotNull
    private Integer userId;
}