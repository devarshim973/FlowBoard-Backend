package com.flowboard.card_service.client;

import com.flowboard.card_service.dto.BulkNotificationRequestDto;
import com.flowboard.card_service.dto.NotificationRequestDto;
import com.flowboard.card_service.dto.NotificationResponseDto;
import com.flowboard.card_service.fallback.NotificationFallback;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "NOTIFICATION-SERVICE", fallback = NotificationFallback.class)
public interface NotificationClient {
    @PostMapping("/api/v1/notifications/send")
    public ResponseEntity<NotificationResponseDto> handleSendNotification(@Valid @RequestBody NotificationRequestDto notificationRequestDto);

    @PostMapping("/api/v1/notifications/bulk")
    public ResponseEntity<List<NotificationResponseDto>> handleSendBulkNotification(@RequestBody BulkNotificationRequestDto dto);
}
