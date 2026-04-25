package com.flowboard.card_service.fallback;

import com.flowboard.card_service.client.WorkspaceClient;
import com.flowboard.card_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkspaceFallback implements WorkspaceClient {
    @Override
    public Boolean isMember(Integer workspaceId, Integer memberId) {
        log.error("CIRCUIT BREAKER - Workspace service unreachable");
        throw new ServiceUnavailableException("Workspace service not available");
    }

    @Override
    public Boolean isPrivate(Integer workspaceId) {
        log.error("CIRCUIT BREAKER - Workspace service unreachable");
        throw new ServiceUnavailableException("Workspace service not available");
    }
}
