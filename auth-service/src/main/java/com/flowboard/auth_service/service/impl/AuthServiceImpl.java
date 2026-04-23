package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.AuthService;
import com.flowboard.auth_service.service.JwtService;
import com.flowboard.auth_service.service.UserOtpService;
import com.flowboard.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final Mapper<SignupDto, User> signupRequestMapper;
    private final Mapper<User, UserDto> userResponseMapper;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserOtpService userOtpService;
    private final UserOtpRepository userOtpRepository;
    private final UserVerificationRepository userVerificationRepository;

    @Override
    public UserDto register(SignupDto signupDto) {
        Optional<User> userOptional = userRepository.findByEmail(signupDto.getEmail());
        if (userOptional.isPresent()) {
            if (userOptional.get().isActive()) {
                throw new UserNotFoundException("User already exist with email " + signupDto.getEmail());
            }
            userRepository.delete(userOptional.get());
            userVerificationRepository.deleteByUserId(userOptional.get().getUserId());
        }

        User user = signupRequestMapper.mapTo(signupDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        User savedUser = userRepository.save(user);

        return userResponseMapper.mapTo(savedUser);
    }

    @Override
    public String login(LoginDto loginDto) {
        log.info("login service called");

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + loginDto.getEmail()));

        if (!user.isActive()) {
            user.setActive(true);
            userRepository.save(user);
            log.info("Activated disabled user {} during login", loginDto.getEmail());
        }

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        log.info("login successful");
        return jwtService.generateToken(loginDto.getEmail());
    }

    @Override
    public void verify(String token) {
        throw new UnsupportedOperationException("Email verification flow is disabled. Accounts are activated immediately.");
    }

    @Override
    public void sendOtp(String email) {
        userOtpService.sendOtp(email);
    }

    @Override
    public void changePassword(ForgetPasswordDto forgetPasswordDto) {
        User user = userRepository.findByEmail(forgetPasswordDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + forgetPasswordDto.getEmail()));

        UserOtp userOtp = userOtpRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new OtpException("No otp for user " + user.getUserId()));

        if (userOtp.getLastOtpDateTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new OtpException("OTP expired");
        }

        if (!Objects.equals(userOtp.getUserId(), user.getUserId())) {
            throw new OtpException("Internal error try resending otp");
        }

        if (!userOtp.getOtp().equals(forgetPasswordDto.getOtp())) {
            throw new OtpException("Invalid otp");
        }

        userOtp.setOtp(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(forgetPasswordDto.getNewPassword()));
        userRepository.save(user);
    }
}