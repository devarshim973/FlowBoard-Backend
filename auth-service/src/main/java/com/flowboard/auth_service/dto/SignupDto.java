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
	@NotBlank(message = "Name must not be blank")
	@Size(min = 3, max = 50, message = "Name must be between 3 to 50 characters")
	private String fullName;

	@NotBlank(message = "Email must not be blank")   // ✅ ADD THIS
	@Email(message = "Enter a valid email address")
	private String email;

	@NotBlank(message = "Password must not be blank")  // ✅ ADD THIS
	@Pattern(
	    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
	    message = "Password must contain 1 lowercase, 1 uppercase, 1 digit, special char, min 8"
	)
	private String password;

	private String otp;

	public SignupDto(String fullName, String email, String password) {
		this.fullName = fullName;
		this.email = email;
		this.password = password;
	}
}
