package com.flowboard.board_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoardMemberRequestDto {
    @NotNull(message = "Board id cannot be null")
    private Integer boardId;

    @NotNull(message = "User id cannot be null")
    private Integer userId;
}