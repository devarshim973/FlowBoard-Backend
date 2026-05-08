package com.flowboard.workspace_service.dto;

import com.flowboard.workspace_service.entity.Visibility;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceResponseDto {
    private Integer workspaceId;

    private String name;

    private String description;

    private Integer ownerId;

    private Visibility visibility;

    private String logoUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}