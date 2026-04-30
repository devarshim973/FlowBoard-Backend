package com.flowboard.payment_service.service;

import com.flowboard.payment_service.dto.CreateOrderResponseDto;
import com.flowboard.payment_service.dto.SubscriptionStatusDto;
import com.flowboard.payment_service.dto.VerifyPaymentRequestDto;

public interface PaymentService {
    CreateOrderResponseDto createOrder(Integer userId);

    SubscriptionStatusDto verifyPayment(Integer userId, VerifyPaymentRequestDto verifyPaymentRequestDto);

    SubscriptionStatusDto getStatus(Integer userId);

    Boolean hasActiveSubscription(Integer userId);
}
