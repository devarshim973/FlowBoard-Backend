package com.flowboard.board_service.fallback;

import com.flowboard.board_service.client.UserClient;
import com.flowboard.board_service.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserFallback implements UserClient {
    @Override
    public List<UserDto> getUserBulk(List<Integer> userIds) {
        log.error("CIRCUIT BREAKER - user client unreachable, return null list");
        return null;
    }
}
