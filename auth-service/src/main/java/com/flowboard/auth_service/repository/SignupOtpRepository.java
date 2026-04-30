package com.flowboard.auth_service.repository;

import com.flowboard.auth_service.entity.SignupOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignupOtpRepository extends JpaRepository<SignupOtp, Integer> {
    Optional<SignupOtp> findByEmail(String email);
}
