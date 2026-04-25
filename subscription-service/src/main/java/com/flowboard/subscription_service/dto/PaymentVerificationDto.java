package com.flowboard.subscription_service.dto;

import com.flowboard.subscription_service.entity.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentVerificationDto {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;

    @NotNull
    private SubscriptionPlan plan;
}
