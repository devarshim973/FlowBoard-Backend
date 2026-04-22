package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.BoardRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardMemberResponseDto {
    private Integer boardMemberId;

    private Integer boardId;

    private Integer userId;

    private BoardRole role;

    private LocalDateTime addedAt;
}