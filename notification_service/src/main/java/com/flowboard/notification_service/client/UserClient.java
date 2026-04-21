package com.flowboard.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE")
public interface UserClient {
    @GetMapping("/api/v1/auth/users/email/{id}")
    String getUserEmail(@PathVariable("id") Integer id);
}
