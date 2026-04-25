package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.AuthService;
import com.flowboard.auth_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Slf4j
@Tag(name = "Auth Controller", description = "Authentication related APIs")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Signup user", description = "Registers a new user")
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signupRequestHandler(@Valid @RequestBody SignupDto signupDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(signupDto));
    }

    @Operation(summary = "Login user", description = "Authenticates user and returns token")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping("/login")
    public ResponseEntity<String> loginRequestHandler(@Valid @RequestBody LoginDto loginDto) {
        log.info("Login request");
        return ResponseEntity.ok().body(authService.login(loginDto));
    }

    @Operation(summary = "Verify account", description = "Verifies user account using token")
    @ApiResponse(responseCode = "202", description = "Account verified")
    @GetMapping("/verify/{token}")
    public ResponseEntity<String> verifyUserAccount(@PathVariable String token) {
        authService.verify(token);
        return ResponseEntity.accepted().body("Account verified successfully, you can login now");
    }

    @Operation(summary = "Send OTP", description = "Sends OTP to email for password reset")
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @PostMapping("/sendotp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
        authService.sendOtp(email);
        return ResponseEntity.ok().body("OTP sent successfully");
    }

    @Operation(summary = "Reset password", description = "Changes password using OTP")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @PostMapping("/forget")
    public ResponseEntity<String> forgetPassword(@Valid @RequestBody ForgetPasswordDto forgetPasswordDto) {
        authService.changePassword(forgetPasswordDto);
        return ResponseEntity.ok().body("Password changed successfully");
    }

    @Operation(summary = "Admin signup", description = "Register as admin")
    @ApiResponse(responseCode = "201", description = "Admin registration request successful")
    @PostMapping("/register-admin")
    public ResponseEntity<UserDto> registerAdmin(@Valid @RequestBody SignupDto signupDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerAdmin(signupDto));
    }

    @GetMapping("/is-admin")
    public ResponseEntity<Boolean> handleIsAdmin(@RequestHeader("X-User-Role") String loggedUserRole) {
        log.info(loggedUserRole);
        return ResponseEntity.ok().body(loggedUserRole.equals("PLATFORM_ADMIN"));
    }
}
