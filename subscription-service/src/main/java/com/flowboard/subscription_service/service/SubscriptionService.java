package com.flowboard.subscription_service.service;

import com.flowboard.subscription_service.dto.PaymentVerificationDto;
import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionPlanResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;
import com.flowboard.subscription_service.dto.SubscriptionResponseDto;

import java.util.List;

public interface SubscriptionService {

    /** Step 1: Create a Razorpay order — does NOT save the subscription yet */
    RazorPayResponseDto buySubscription(SubscriptionRequestDto subscriptionRequestDto, Integer userId);

    /** Step 2: Verify Razorpay signature and activate the subscription */
    SubscriptionResponseDto verifyAndActivate(PaymentVerificationDto verificationDto, Integer userId);

    SubscriptionResponseDto getDetails(Integer userId);

    List<SubscriptionPlanResponseDto> getPlanDetails();
}
