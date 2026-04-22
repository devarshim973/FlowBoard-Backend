package com.flowboard.board_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserDto {
    @Schema(description = "Full name", example = "user1")
    private String fullName;

    @Schema(description = "Email", example = "user1@gmail.com")
    private String email;

    @Schema(description = "Profile avatar URL", example = "url")
    private String avatarUrl;

    @Schema(description = "User ID", example = "1")
    private Integer userId;
}
