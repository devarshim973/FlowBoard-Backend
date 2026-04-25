package com.flowboard.comment_service.client;

import com.flowboard.comment_service.fallback.CardFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CARD-SERVICE", fallback = CardFallback.class)
public interface CardClient {
    @GetMapping("/api/v1/cards/assigned-user/{cardId}")
    public Integer getAssignedUserId(@PathVariable(value = "cardId") Integer cardId);
}
