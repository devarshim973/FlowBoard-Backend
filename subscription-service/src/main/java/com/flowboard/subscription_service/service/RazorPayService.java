package com.flowboard.subscription_service.service;

import com.flowboard.subscription_service.dto.RazorPayResponseDto;
import com.flowboard.subscription_service.dto.SubscriptionRequestDto;

public interface RazorPayService {
    public RazorPayResponseDto getSubscription(SubscriptionRequestDto subscriptionRequestDto);
}
