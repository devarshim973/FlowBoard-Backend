package com.flowboard.board_service.dto;

import com.flowboard.board_service.entity.BoardRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoardMemberResponseDto {
    @Schema(description = "Board member record ID", example = "1")
    private Integer boardMemberId;

    @Schema(description = "Board ID", example = "1")
    private Integer boardId;

    @Schema(description = "User ID", example = "2")
    private Integer userId;

    @Schema(description = "Role of member in board", example = "MEMBER")
    private BoardRole role;

    @Schema(description = "Date and time when member was added")
    private LocalDateTime addedAt;
}