package com.flowboard.board_service.fallback;

import com.flowboard.board_service.client.WorkspaceClient;
import com.flowboard.board_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WorkspaceFallback implements WorkspaceClient {
    @Override
    public Integer getOwnerId(Integer id) {
        log.error("CIRCUIT BREAKER - workspace client unreachable");
        throw new ServiceUnavailableException("User service not available");
    }

    @Override
    public Boolean isMember(Integer workspaceId, Integer memberId) {
        log.error("CIRCUIT BREAKER - workspace client unreachable");
        throw new ServiceUnavailableException("User service not available");
    }

    @Override
    public Boolean isPrivate(Integer workspaceId) {
        log.error("CIRCUIT BREAKER - workspace client unreachable");
        throw new ServiceUnavailableException("User service not available");
    }
}
