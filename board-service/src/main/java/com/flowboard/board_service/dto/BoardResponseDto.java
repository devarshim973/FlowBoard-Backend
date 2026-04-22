package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.Visibility;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardResponseDto {
    private Integer boardId;

    private Integer workspaceId;

    private String name;

    private String description;

    private String background;

    private Visibility visibility;

    private Integer createdById;

    private boolean isClosed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}