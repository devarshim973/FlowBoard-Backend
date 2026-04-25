package com.flowboard.subscription_service.dto;

import com.flowboard.subscription_service.entity.SubscriptionPlan;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionResponseDto {
    private Integer id;

    private Integer userId;

    private SubscriptionPlan plan;

    private LocalDate startDate;

    private LocalDate expiryDate;

    private String status;
}
