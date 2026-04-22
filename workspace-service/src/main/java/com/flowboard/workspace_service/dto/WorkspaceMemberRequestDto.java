package com.flowboard.workspace_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkspaceMemberRequestDto {
    @NotNull
    private Integer workspaceId;

    @NotNull
    private Integer userId;
}