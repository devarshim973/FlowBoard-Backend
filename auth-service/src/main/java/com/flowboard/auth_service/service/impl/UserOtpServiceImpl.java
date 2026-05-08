package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.entity.SignupOtp;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.SignupOtpRepository;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.UserOtpService;
import com.flowboard.auth_service.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOtpServiceImpl implements UserOtpService {
    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final SignupOtpRepository signupOtpRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void sendOtp(String email) {
        log.info("OTP send requested for email {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User does not exist with email " + email));

        Optional<UserOtp> userOtpOptional = userOtpRepository.findByUserId(user.getUserId());

        String otp = UUID.randomUUID().toString().substring(0, 6);
        if(userOtpOptional.isEmpty()) {
            UserOtp userOtp = UserOtp.builder()
                    .userId(user.getUserId())
                    .otp(otp)
                    .otpSent(1)
                    .build();
            userOtpRepository.save(userOtp);
            emailService.sendOtpEmail(email, otp);
            log.info("OTP sent to user {}", user.getUserId());
            return;
        }
        else {
            UserOtp userOtp = userOtpOptional.get();
            LocalDateTime now = LocalDateTime.now();

            if(userOtp.getLastOtpDateTime().plusSeconds(AppConstants.otpResendCooldownSeconds).isAfter(now)) {
                log.warn("OTP resend blocked for user {}", user.getUserId());
                throw new OtpException("You can send new OTP after 10 seconds");
            }
            if(userOtp.getOtpSent() >= AppConstants.otpLimit) {
                log.warn("OTP limit reached for user {}", user.getUserId());
                throw new OtpException("Maximum OTP limit reached please try again when limit reset - tomorrow");
            }

            userOtp.setOtpSent(userOtp.getOtpSent()+1);
            userOtp.setOtp(otp);
            userOtpRepository.save(userOtp);
            emailService.sendOtpEmail(user.getEmail(), otp);
            log.info("OTP resent to user {}", user.getUserId());
            return;
        }
    }

    @Override
    @Transactional
    public void sendSignupOtp(String email) {
        log.info("Signup OTP send requested for email {}", email);

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (existingUser.isActive()) {
                throw new OtpException("User already exist with email " + email);
            }
        });

        Optional<SignupOtp> signupOtpOptional = signupOtpRepository.findByEmail(email);
        String otp = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

        if (signupOtpOptional.isEmpty()) {
            SignupOtp signupOtp = SignupOtp.builder()
                    .email(email)
                    .otp(otp)
                    .otpSent(1)
                    .build();
            signupOtpRepository.save(signupOtp);
            emailService.sendSignupOtpEmail(email, otp);
            return;
        }

        SignupOtp signupOtp = signupOtpOptional.get();
        LocalDateTime now = LocalDateTime.now();

        if (signupOtp.getLastOtpDateTime() != null && signupOtp.getLastOtpDateTime().plusSeconds(AppConstants.otpResendCooldownSeconds).isAfter(now)) {
            throw new OtpException("You can send new OTP after 10 seconds");
        }

        if (signupOtp.getOtpSent() >= AppConstants.otpLimit) {
            throw new OtpException("Maximum OTP limit reached please try again tomorrow");
        }

        signupOtp.setOtpSent(signupOtp.getOtpSent() + 1);
        signupOtp.setOtp(otp);
        signupOtpRepository.save(signupOtp);
        emailService.sendSignupOtpEmail(email, otp);
    }

    @Override
    public void validateSignupOtp(String email, String otp) {
        SignupOtp signupOtp = signupOtpRepository.findByEmail(email)
                .orElseThrow(() -> new OtpException("No signup OTP found for " + email));

        if (signupOtp.getLastOtpDateTime() == null || signupOtp.getLastOtpDateTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
            throw new OtpException("OTP expired");
        }

        if (!signupOtp.getOtp().equalsIgnoreCase(otp)) {
            throw new OtpException("Invalid otp");
        }

        signupOtpRepository.delete(signupOtp);
    }
}
