package com.flowboard.board_service.client;

import com.flowboard.board_service.dto.UserDto;
import com.flowboard.board_service.fallback.UserFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "AUTH-SERVICE", fallback = UserFallback.class)
public interface UserClient {
    @GetMapping("api/v1/user/bulk")
    public List<UserDto> getUserBulk(@RequestParam List<Integer> userIds);
}
