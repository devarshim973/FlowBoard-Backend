package com.flowboard.admin_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "BOARD-SERVICE", url = "${services.board.url:http://localhost:8085}")
public interface BoardAdminClient {
    @GetMapping("/api/v1/admin/boards")
    Object getBoards(@RequestHeader("X-User-Role") String role);

    @DeleteMapping("/api/v1/admin/boards/{boardId}")
    String deleteBoard(@RequestHeader("X-User-Role") String role, @PathVariable("boardId") Integer boardId);
}
