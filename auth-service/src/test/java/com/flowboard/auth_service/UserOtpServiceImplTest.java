package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.impl.UserOtpServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class UserOtpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOtpRepository userOtpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserOtpServiceImpl userOtpService;

    @Test
    void sendOtp_withNewUserOtp_createsOtpAndSendsMail() {

        User user = new User();
        user.setUserId(1);
        user.setEmail("john@gmail.com");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.empty());

        userOtpService.sendOtp("john@gmail.com");

        verify(userOtpRepository).save(any(UserOtp.class));
        verify(emailService).sendOtpEmail(any(), any());
    }

    @Test
    void sendOtp_withWrongEmail_throwsException() {

        when(userRepository.findByEmail("wrong@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userOtpService.sendOtp("wrong@gmail.com"));
    }

    @Test
    void sendOtp_withExistingOtpAfter5Min_updatesOtp() {

        User user = new User();
        user.setUserId(1);
        user.setEmail("john@gmail.com");

        UserOtp userOtp = new UserOtp();
        userOtp.setUserId(1);
        userOtp.setOtpSent(1);
        userOtp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.of(userOtp));

        userOtpService.sendOtp("john@gmail.com");

        verify(userOtpRepository).save(userOtp);
        verify(emailService).sendOtpEmail(any(), any());
    }

    @Test
    void sendOtp_withOtpBefore5Min_throwsException() {

        User user = new User();
        user.setUserId(1);
        user.setEmail("john@gmail.com");

        UserOtp userOtp = new UserOtp();
        userOtp.setUserId(1);
        userOtp.setOtpSent(1);
        userOtp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.of(userOtp));

        assertThrows(OtpException.class,
                () -> userOtpService.sendOtp("john@gmail.com"));
    }

    @Test
    void sendOtp_withOtpLimitReached_throwsException() {

        User user = new User();
        user.setUserId(1);
        user.setEmail("john@gmail.com");

        UserOtp userOtp = new UserOtp();
        userOtp.setUserId(1);
        userOtp.setOtpSent(5);
        userOtp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userOtpRepository.findByUserId(1))
                .thenReturn(Optional.of(userOtp));

        assertThrows(OtpException.class,
                () -> userOtpService.sendOtp("john@gmail.com"));
    }
}