package com.flowboard.workspace_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceMemberResponseDto {
    private Integer memberId;

    private Integer workspaceId;

    private Integer userId;

    private LocalDateTime joinedAt;
}