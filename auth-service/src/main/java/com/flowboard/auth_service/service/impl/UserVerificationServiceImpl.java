package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.entity.UserVerification;
import com.flowboard.auth_service.exception.TokenNotFoundException;
import com.flowboard.auth_service.repository.UserVerificationRepository;
import com.flowboard.auth_service.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVerificationServiceImpl implements UserVerificationService {
    private final UserVerificationRepository userVerificationRepository;
    @Override
    public UserVerification save(UserVerification userVerification) {
        return userVerificationRepository.save(userVerification);
    }

    @Override
    public void deleteByUserId(Integer userId) {
        UserVerification userVerification = userVerificationRepository.findByUserId(userId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with id " + userId));
        userVerificationRepository.delete(userVerification);
    }

    @Override
    public UserVerification findByToken(String token) {
        return userVerificationRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Token " + token + " not found!"));
    }
}
