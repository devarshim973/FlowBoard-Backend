package com.flowboard.workspace_service.fallback;

import com.flowboard.workspace_service.client.PaymentClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentFallback implements PaymentClient {
    @Override
    public Boolean hasActiveSubscription(Integer userId) {
        log.warn("CIRCUIT BREAKER - payment client unreachable for user {}. Falling back to free-plan response.", userId);
        return false;
    }
}
