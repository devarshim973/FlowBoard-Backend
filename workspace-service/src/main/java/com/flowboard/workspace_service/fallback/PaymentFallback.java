package com.flowboard.workspace_service.fallback;

import com.flowboard.workspace_service.client.PaymentClient;
import com.flowboard.workspace_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentFallback implements PaymentClient {
    @Override
    public Boolean hasActiveSubscription(Integer userId) {
        log.error("CIRCUIT BREAKER - payment client unreachable for user {}", userId);
        throw new ServiceUnavailableException("Payment service not available");
    }
}
