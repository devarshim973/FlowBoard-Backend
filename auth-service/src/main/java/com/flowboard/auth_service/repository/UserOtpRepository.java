package com.flowboard.auth_service.repository;

import com.flowboard.auth_service.entity.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOtpRepository extends JpaRepository<UserOtp, Integer> {
    Optional<UserOtp> findByUserId(Integer userId);
}
