package com.flowboard.workspace_service.client;

import com.flowboard.workspace_service.fallback.PaymentFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PAYMENT-SERVICE", url = "${services.payment.url:http://localhost:8089}", fallback = PaymentFallback.class)
public interface PaymentClient {
    @GetMapping("/api/v1/payments/check/{userId}")
    Boolean hasActiveSubscription(@PathVariable("userId") Integer userId);
}
