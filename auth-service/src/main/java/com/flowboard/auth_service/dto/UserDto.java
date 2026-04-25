package com.flowboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    @Schema(description = "Full name", example = "user1")
    private String fullName;

    @Schema(description = "Email", example = "user1@gmail.com")
    private String email;

    @Schema(description = "Profile avatar URL", example = "url")
    private String avatarUrl;

    @Schema(description = "User ID", example = "1")
    private Integer userId;

    private boolean isActive;
}
