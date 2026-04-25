package com.flowboard.subscription_service.dto;

import com.flowboard.subscription_service.entity.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SubscriptionRequestDto {
    @NotNull
    private SubscriptionPlan plan;
}
