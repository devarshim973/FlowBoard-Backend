package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.UserOtpService;
import com.flowboard.auth_service.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserOtpServiceImpl implements UserOtpService {
    private final UserRepository userRepository;
    private final UserOtpRepository userOtpRepository;
    private final EmailService emailService;
    @Override
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
        }
        else {
            UserOtp userOtp = userOtpOptional.get();
            LocalDateTime now = LocalDateTime.now();

            if(userOtp.getLastOtpDateTime().plusMinutes(5).isAfter(now)) {
                log.warn("OTP resend blocked for user {}", user.getUserId());
                throw new OtpException("You can send new OTP after 5 minutes");
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
        }
    }
}
