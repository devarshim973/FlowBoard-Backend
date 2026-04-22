package com.flowboard.comment_service.client;

import com.flowboard.comment_service.dto.BulkNotificationRequestDto;
import com.flowboard.comment_service.dto.NotificationResponseDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationClient {
    @PostMapping("/api/v1/notifications/bulk")
    public ResponseEntity<List<NotificationResponseDto>> sendBulk(@RequestBody BulkNotificationRequestDto dto);
}
