package com.flowboard.subscription_service.mapper.impl;

import com.flowboard.subscription_service.dto.SubscriptionResponseDto;
import com.flowboard.subscription_service.entity.UserSubscription;
import com.flowboard.subscription_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SubscriptionResponseMapper implements Mapper<UserSubscription, SubscriptionResponseDto> {
    private final ModelMapper modelMapper;
    @Override
    public SubscriptionResponseDto mapTo(UserSubscription userSubscription) {
        return modelMapper.map(userSubscription, SubscriptionResponseDto.class);
    }

    @Override
    public UserSubscription mapFrom(SubscriptionResponseDto subscriptionResponseDto) {
        return modelMapper.map(subscriptionResponseDto, UserSubscription.class);
    }
}
