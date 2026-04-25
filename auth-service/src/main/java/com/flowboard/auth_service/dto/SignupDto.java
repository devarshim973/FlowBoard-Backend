package com.flowboard.auth_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {
    @Schema(description = "Full name of user", example = "User1")
    @NotBlank(message = "Name must not be blank")
    @Size(min = 3, max = 50, message = "Name must be between 3 to 50 character")
    private String fullName;

    @Schema(description = "User email", example = "user1@gmail.com")
    @Email(message = "Enter a valid email address")
    private String email;

    @Schema(description = "Password (min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char)",
            example = "Password@1")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    message = "Password must contain 1 lowercase, 1 uppercase, 1 digit, size 8")
    private String password;
}
