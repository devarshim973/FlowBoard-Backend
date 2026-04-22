package com.flowboard.comment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "AUTH-SERVICE")
public interface UserClient {
    @GetMapping("/api/v1/user/findAll")
    public List<Integer> getUserIdsByUsername(@RequestParam List<String> userEmailList);
}
