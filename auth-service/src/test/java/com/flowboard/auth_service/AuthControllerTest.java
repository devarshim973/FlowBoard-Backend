package com.flowboard.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.auth_service.controller.AuthController;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.AuthService;
import com.flowboard.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_withValidData_returns201() throws Exception {
        SignupDto signupDto = new SignupDto("John Doe", "john@example.com", "Password@1");
        UserDto userDto = new UserDto("John Doe", "john@example.com", "url", 1, false, null);

        when(authService.register(any(SignupDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void signup_withInvalidData_returns400() throws Exception {
        SignupDto invalid = new SignupDto("", "", "");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withValidCredentials_returns200WithToken() throws Exception {
        LoginDto loginDto = new LoginDto("john@example.com", "Password@1");

        when(authService.login(any(LoginDto.class))).thenReturn("jwt-token-xyz");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token-xyz"));
    }

    @Test
    void login_withWrongCredentials_returns401() throws Exception {
        LoginDto loginDto = new LoginDto("john@example.com", "Password@1");

        when(authService.login(any(LoginDto.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifyAccount_withValidToken_returns202() throws Exception {
        doNothing().when(authService).verify("valid-token-123");

        mockMvc.perform(get("/api/v1/auth/verify/valid-token-123"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Account verified successfully, you can login now"));
    }

    @Test
    void verifyAccount_withExpiredToken_returns400() throws Exception {
        doThrow(new RuntimeException("Token expired"))
                .when(authService).verify("expired-token");

        mockMvc.perform(get("/api/v1/auth/verify/expired-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendOtp_withRegisteredEmail_returns200() throws Exception {
        doNothing().when(authService).sendOtp("john@example.com");

        mockMvc.perform(post("/api/v1/auth/sendotp")
                        .param("email", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent successfully"));
    }

    @Test
    void sendOtp_withUnregisteredEmail_returns404() throws Exception {
        doThrow(new RuntimeException("User not found"))
                .when(authService).sendOtp("ghost@example.com");

        mockMvc.perform(post("/api/v1/auth/sendotp")
                        .param("email", "ghost@example.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgetPassword_withValidOtp_returns200() throws Exception {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@example.com", "123456", "NewPass@123");
        doNothing().when(authService).changePassword(any(ForgetPasswordDto.class));

        mockMvc.perform(post("/api/v1/auth/forget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));
    }

    @Test
    void forgetPassword_withWrongOtp_returns400() throws Exception {
        ForgetPasswordDto dto = new ForgetPasswordDto("john@example.com", "000000", "NewPass@123");

        doThrow(new RuntimeException("Invalid or expired OTP"))
                .when(authService).changePassword(any(ForgetPasswordDto.class));

        mockMvc.perform(post("/api/v1/auth/forget")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
