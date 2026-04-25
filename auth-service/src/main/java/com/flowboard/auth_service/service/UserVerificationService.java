package com.flowboard.auth_service.service;

import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.entity.UserVerification;

public interface UserVerificationService {
    public UserVerification save(UserVerification userVerification);

    public void deleteByUserId(Integer userId);

    public UserVerification findByToken(String token);
}
