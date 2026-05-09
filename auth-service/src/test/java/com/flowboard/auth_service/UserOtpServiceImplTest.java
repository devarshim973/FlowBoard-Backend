package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.SignupOtp;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserOtp;
import com.flowboard.auth_service.exception.OtpException;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.SignupOtpRepository;
import com.flowboard.auth_service.repository.UserOtpRepository;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.impl.UserOtpServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOtpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOtpRepository userOtpRepository;
    @Mock
    private SignupOtpRepository signupOtpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserOtpServiceImpl userOtpService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1);
        user.setEmail("john@gmail.com");
        user.setActive(true);
    }

    @Test
    void sendOtp_withNewUserOtp_createsOtpAndSendsMail() {
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

    @Test
    void sendSignupOtp_withActiveUser_throwsException() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));

        assertThrows(OtpException.class, () -> userOtpService.sendSignupOtp("john@gmail.com"));
    }

    @Test
    void sendSignupOtp_withNewEmail_createsOtpAndSendsMail() {
        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());

        userOtpService.sendSignupOtp("new@gmail.com");

        verify(signupOtpRepository).save(any(SignupOtp.class));
        verify(emailService).sendSignupOtpEmail(eq("new@gmail.com"), any());
    }

    @Test
    void sendSignupOtp_withCooldownActive_throwsException() {
        SignupOtp signupOtp = new SignupOtp();
        signupOtp.setEmail("new@gmail.com");
        signupOtp.setOtpSent(1);
        signupOtp.setLastOtpDateTime(LocalDateTime.now());

        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.of(signupOtp));

        assertThrows(OtpException.class, () -> userOtpService.sendSignupOtp("new@gmail.com"));
    }

    @Test
    void sendSignupOtp_withOtpLimitReached_throwsException() {
        SignupOtp signupOtp = new SignupOtp();
        signupOtp.setEmail("new@gmail.com");
        signupOtp.setOtpSent(5);
        signupOtp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.of(signupOtp));

        assertThrows(OtpException.class, () -> userOtpService.sendSignupOtp("new@gmail.com"));
    }

    @Test
    void sendSignupOtp_withExistingOtp_updatesAndSendsMail() {
        SignupOtp signupOtp = new SignupOtp();
        signupOtp.setEmail("new@gmail.com");
        signupOtp.setOtpSent(1);
        signupOtp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.of(signupOtp));

        userOtpService.sendSignupOtp("new@gmail.com");

        verify(signupOtpRepository).save(signupOtp);
        verify(emailService).sendSignupOtpEmail(eq("new@gmail.com"), any());
    }

    @Test
    void validateSignupOtp_withMissingRecord_throwsException() {
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());

        assertThrows(OtpException.class, () -> userOtpService.validateSignupOtp("new@gmail.com", "ABC123"));
    }

    @Test
    void validateSignupOtp_withExpiredOtp_throwsException() {
        SignupOtp signupOtp = new SignupOtp();
        signupOtp.setEmail("new@gmail.com");
        signupOtp.setOtp("ABC123");
        signupOtp.setLastOtpDateTime(LocalDateTime.now().minusMinutes(10));
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.of(signupOtp));

        assertThrows(OtpException.class, () -> userOtpService.validateSignupOtp("new@gmail.com", "ABC123"));
    }

    @Test
    void validateSignupOtp_withWrongOtp_throwsException() {
        SignupOtp signupOtp = new SignupOtp();
        signupOtp.setEmail("new@gmail.com");
        signupOtp.setOtp("ABC123");
        signupOtp.setLastOtpDateTime(LocalDateTime.now());
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.of(signupOtp));

        assertThrows(OtpException.class, () -> userOtpService.validateSignupOtp("new@gmail.com", "ZZZ999"));
    }

    @Test
    void validateSignupOtp_withValidOtp_deletesRecord() {
        SignupOtp signupOtp = new SignupOtp();
        signupOtp.setEmail("new@gmail.com");
        signupOtp.setOtp("ABC123");
        signupOtp.setLastOtpDateTime(LocalDateTime.now());
        when(signupOtpRepository.findByEmail("new@gmail.com")).thenReturn(Optional.of(signupOtp));

        assertDoesNotThrow(() -> userOtpService.validateSignupOtp("new@gmail.com", "abc123"));

        verify(signupOtpRepository).delete(signupOtp);
    }
}
