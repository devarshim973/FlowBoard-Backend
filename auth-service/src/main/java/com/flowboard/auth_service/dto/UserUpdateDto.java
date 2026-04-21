package com.flowboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Schema(description = "Updated full name", example = "User1")
    @NotBlank
    @Size(min = 3, max = 50)
    private String fullName;

    @Schema(description = "Updated avatar URL", example = "url")
    private String avatarUrl;
}
