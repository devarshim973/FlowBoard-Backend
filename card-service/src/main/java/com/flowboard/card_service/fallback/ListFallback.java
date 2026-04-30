package com.flowboard.card_service.fallback;

import com.flowboard.card_service.client.ListClient;
import com.flowboard.card_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListFallback implements ListClient {
    @Override
    public Integer getBoardId(Integer listId) {
        log.error("CIRCUIT BREAKER - List service unreachable");
        throw new ServiceUnavailableException("List service not available");
    }
}
