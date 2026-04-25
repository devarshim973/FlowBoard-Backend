package com.flowboard.list_service.client;

import com.flowboard.list_service.fallback.BoardFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "BOARD-SERVICE", fallback = BoardFallback.class)
public interface BoardClient {
    @GetMapping("/api/v1/board-members/{boardId}/is-member/{userId}")
    public Boolean isMember(
            @PathVariable(value = "boardId") Integer boardId,
            @PathVariable(value = "userId") Integer userId);

    @GetMapping("/api/v1/boards/workspace/{boardId}")
    public Integer getWorkspaceId(@PathVariable(value = "boardId") Integer boardId);

    @GetMapping("/api/v1/boards/is-private/{boardId}")
    public Boolean isPrivate(@PathVariable(value = "boardId") Integer boardId);
}
