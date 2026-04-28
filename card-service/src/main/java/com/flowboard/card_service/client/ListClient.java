package com.flowboard.card_service.client;

import com.flowboard.card_service.fallback.ListFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "List-Service", fallback = ListFallback.class)
public interface ListClient {
    @GetMapping("/api/v1/lists/get-boardId/{listId}")
    public Integer getBoardId(@PathVariable(value = "listId") Integer listId);
}
