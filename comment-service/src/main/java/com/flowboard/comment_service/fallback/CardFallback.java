package com.flowboard.comment_service.fallback;

import com.flowboard.comment_service.client.CardClient;
import com.flowboard.comment_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardFallback implements CardClient {
    @Override
    public Integer getAssignedUserId(Integer cardId) {
        log.error("CIRCUIT BREAKER - Card service unreachable");
        throw new ServiceUnavailableException("Card service not available");
    }
}
