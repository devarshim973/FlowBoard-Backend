package com.flowboard.notification_service.client;

import com.flowboard.notification_service.fallback.UserFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTH-SERVICE", url = "${services.auth.url:http://localhost:8081}", fallback = UserFallback.class)
public interface UserClient {
    @GetMapping("/api/v1/user/email/{id}")
    String getUserEmail(@PathVariable("id") Integer id);
}
