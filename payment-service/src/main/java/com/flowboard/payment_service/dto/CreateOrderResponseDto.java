package com.flowboard.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponseDto {
    private String key;
    private String orderId;
    private Integer amount;
    private String currency;
    private String planName;
    private Integer userId;
    private String description;
}
