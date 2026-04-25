package com.flowboard.notification_service.fallback;

import com.flowboard.notification_service.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserFallback implements UserClient {
    @Override
    public String getUserEmail(Integer userId) {
        log.info("CIRCUIT BREAKER - User client unreachable, returning an empty string");
        return "";
    }
}
