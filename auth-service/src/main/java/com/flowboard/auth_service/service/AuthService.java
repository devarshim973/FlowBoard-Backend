package com.flowboard.auth_service.service;

import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.User;

public interface AuthService {
    public UserDto register(SignupDto signupDto);

    public String login(LoginDto loginDto);

    public void changePassword(ForgetPasswordDto forgetPasswordDto);

    void verify(String token);

    void sendOtp(String email);

    UserDto registerAdmin(SignupDto signupDto);
}
