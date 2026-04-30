package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.TokenNotFoundException;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.impl.UserVerificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserVerificationServiceImplTest {

    @Mock
    private UserVerificationRepository userVerificationRepository;

    @InjectMocks
    private UserVerificationServiceImpl userVerificationService;

    @Test
    void save_withValidData_returnsSavedEntity() {

        UserVerification verification =
                UserVerification.builder()
                        .userId(1)
                        .token("abc123")
                        .build();

        when(userVerificationRepository.save(verification))
                .thenReturn(verification);

        UserVerification result =
                userVerificationService.save(verification);

        assertEquals("abc123", result.getToken());
    }

    @Test
    void deleteByUserId_withValidId_deletesRecord() {

        UserVerification verification =
                UserVerification.builder()
                        .userId(1)
                        .token("abc123")
                        .build();

        when(userVerificationRepository.findByUserId(1))
                .thenReturn(Optional.of(verification));

        userVerificationService.deleteByUserId(1);

        verify(userVerificationRepository).delete(verification);
    }

    @Test
    void deleteByUserId_withWrongId_throwsException() {

        when(userVerificationRepository.findByUserId(99))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userVerificationService.deleteByUserId(99));
    }

    @Test
    void findByToken_withValidToken_returnsEntity() {

        UserVerification verification =
                UserVerification.builder()
                        .userId(1)
                        .token("token123")
                        .build();

        when(userVerificationRepository.findByToken("token123"))
                .thenReturn(Optional.of(verification));

        UserVerification result =
                userVerificationService.findByToken("token123");

        assertEquals("token123", result.getToken());
    }

    @Test
    void findByToken_withWrongToken_throwsException() {

        when(userVerificationRepository.findByToken("wrong"))
                .thenReturn(Optional.empty());

        assertThrows(TokenNotFoundException.class,
                () -> userVerificationService.findByToken("wrong"));
    }
}