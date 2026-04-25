package com.flowboard.board_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoardMemberRequestDto {
    @Schema(description = "Board ID", example = "1")
    @NotNull(message = "Board id cannot be null")
    private Integer boardId;

    @Schema(description = "User ID to add as member", example = "2")
    @NotNull(message = "User id cannot be null")
    private Integer userId;
}