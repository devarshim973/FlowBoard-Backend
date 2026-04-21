package com.flowboard.auth_service.repository;

import com.flowboard.auth_service.entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, Integer> {
    Optional<UserVerification> findByToken(String token);

    Optional<UserVerification> findByUserId(Integer userId);

    void deleteByUserId(Integer userId);
}
