package com.flowboard.payment_service.repository;

import com.flowboard.payment_service.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Integer> {
    Optional<UserSubscription> findByUserId(Integer userId);

    Optional<UserSubscription> findByUserIdAndRazorpayOrderId(Integer userId, String razorpayOrderId);
}
