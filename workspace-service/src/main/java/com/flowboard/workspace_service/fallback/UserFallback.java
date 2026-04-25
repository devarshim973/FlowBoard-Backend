package com.flowboard.workspace_service.fallback;

import com.flowboard.workspace_service.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFallback implements UserClient {
    @Override
    public Boolean checkUser(Integer userId) {
        log.info("CIRCUIT BREAKER - User client unreachable, returning false");
        return false;
    }
}
