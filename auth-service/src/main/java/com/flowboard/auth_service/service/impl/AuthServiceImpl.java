package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.Mapper.impl.SignupRequestMapper;
import com.flowboard.auth_service.Mapper.impl.UserResponseMapper;
import com.flowboard.auth_service.dto.ForgetPasswordDto;
import com.flowboard.auth_service.dto.LoginDto;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.AuthService;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.JwtService;
import com.flowboard.auth_service.service.UserOtpService;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final SignupRequestMapper signupRequestMapper;
    private final UserResponseMapper userResponseMapper;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserVerificationService userVerificationService;
    private final EmailService emailService;
    private final UserService userService;
    private final UserOtpService userOtpService;
    private final UserOtpRepository userOtpRepository;
    private final UserVerificationRepository userVerificationRepository;

    @Value("${domain.url}")
    private String url;

    @Override
    @Transactional
    public UserDto register(SignupDto signupDto) {
        log.info("User signup requested for email {}", signupDto.getEmail());

        Optional<User> userOptional = userRepository.findByEmail(signupDto.getEmail());

        if (userOptional.isPresent()) {
            if (userOptional.get().isActive()) {
                throw new UserNotFoundException("User already exist with email " + signupDto.getEmail());
            }

            userRepository.delete(userOptional.get());
            userVerificationRepository.deleteByUserId(userOptional.get().getUserId());
        }

        if (signupDto.getOtp() == null || signupDto.getOtp().isBlank()) {
            throw new OtpException("Signup OTP is required");
        }

        userOtpService.validateSignupOtp(signupDto.getEmail(), signupDto.getOtp());

        User user = signupRequestMapper.mapTo(signupDto);
        user.setRole(ROLE.USER);
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return userResponseMapper.mapTo(savedUser);
    }

    @Override
    @Transactional
    public UserDto registerAdmin(SignupDto signupDto) {
        log.info("Admin signup requested for email {}", signupDto.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(signupDto.getEmail());
        if (userOptional.isPresent()) {
            log.warn("Admin signup rejected because user already exists for email {}", signupDto.getEmail());
            throw new UserNotFoundException("User already exist with email " + signupDto.getEmail());
        }

        User user = signupRequestMapper.mapTo(signupDto);
        user.setRole(ROLE.PLATFORM_ADMIN);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        UserVerification userVerification = UserVerification.builder()
                .userId(savedUser.getUserId())
                .token(token)
                .build();

        UserVerification savedUserVerification = userVerificationService.save(userVerification);

        emailService.sendVerificationEmailForAdmin(user.getEmail(), url + "auth/verify/" + savedUserVerification.getToken());
        log.info("Admin signup completed for user {}", savedUser.getUserId());

        return userResponseMapper.mapTo(savedUser);
    }

    @Override
    public String login(LoginDto loginDto) {
        log.info("Login requested for email {}", loginDto.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );
        log.info("Login successful for email {}", loginDto.getEmail());
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email"));
        return jwtService.generateToken(loginDto.getEmail(), user.getRole().toString(), user.getUserId());
    }

    @Override
    @Transactional
    public void verify(String token) {
        UserVerification userVerification = userVerificationService.findByToken(token);
        User user = userService.findById(userVerification.getUserId());
        log.info("Account verification requested for user {}", user.getUserId());
        userVerificationService.deleteByUserId(user.getUserId());
        user.setActive(true);
        userRepository.save(user);
        log.info("Account verified for user {}", user.getUserId());
    }

    @Override
    public String sendOtp(String email) {
        log.info("Password reset OTP requested for email {}", email);
        return userOtpService.sendOtp(email);
    }

    @Override
    public String sendSignupOtp(String email) {
        log.info("Signup OTP requested for email {}", email);
        return userOtpService.sendSignupOtp(email);
    }

    @Override
    @Transactional
    public void changePassword(ForgetPasswordDto forgetPasswordDto) {
        log.info("Password reset requested for email {}", forgetPasswordDto.getEmail());
        User user = userRepository.findByEmail(forgetPasswordDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + forgetPasswordDto.getEmail()));

        UserOtp userOtp = userOtpRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new OtpException("No otp for user " + user.getUserId()));

        if (userOtp.getLastOtpDateTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
            log.warn("Password reset failed due to expired OTP for email {}", forgetPasswordDto.getEmail());
            throw new OtpException("OTP expired");
        }

        if (!Objects.equals(userOtp.getUserId(), user.getUserId())) {
            log.warn("Password reset failed due to OTP ownership mismatch for user {}", user.getUserId());
            throw new OtpException("Internal error try resending otp");
        }

        if (!userOtp.getOtp().equals(forgetPasswordDto.getOtp())) {
            log.warn("Password reset failed due to invalid OTP for email {}", forgetPasswordDto.getEmail());
            throw new OtpException("Invalid otp");
        }

        userOtp.setOtp(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(forgetPasswordDto.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset completed for user {}", user.getUserId());
    }
}
