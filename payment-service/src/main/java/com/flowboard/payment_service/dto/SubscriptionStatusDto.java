package com.flowboard.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusDto {
    private Integer userId;
    private Boolean active;
    private String planName;
    private Integer amount;
    private String currency;
    private Integer freeWorkspaceLimit;
}
