package com.flowboard.subscription_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RazorPayResponseDto {
    private String status;

    private String message;

    private String orderId;

    private String keyId;

    private String amount;

    private String currency;
}