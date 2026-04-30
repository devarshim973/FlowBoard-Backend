package com.flowboard.card_service.fallback;

import com.flowboard.card_service.client.BoardClient;
import com.flowboard.card_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BoardFallback implements BoardClient {
    @Override
    public Boolean isMember(Integer boardId, Integer userId) {
        log.error("CIRCUIT BREAKER - Board service unreachable");
        throw new ServiceUnavailableException("Board service not available");
    }

    @Override
    public Integer getWorkspaceId(Integer boardId) {
        log.error("CIRCUIT BREAKER - Board service unreachable");
        throw new ServiceUnavailableException("Board service not available");
    }

    @Override
    public Boolean isPrivate(Integer boardId) {
        log.error("CIRCUIT BREAKER - Board service unreachable");
        throw new ServiceUnavailableException("Board service not available");
    }
}
