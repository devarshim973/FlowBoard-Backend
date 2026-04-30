package com.flowboard.comment_service.fallback;

import com.flowboard.comment_service.client.UserClient;
import com.flowboard.comment_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserFallback implements UserClient {
    @Override
    public List<Integer> getUserIdsByUsername(List<String> userEmailList) {
        log.error("CIRCUIT BREAKER - User service unreachable");
        throw new ServiceUnavailableException("User service not available");
    }
}
