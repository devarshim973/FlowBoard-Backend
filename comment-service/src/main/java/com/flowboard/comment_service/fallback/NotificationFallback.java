package com.flowboard.comment_service.fallback;

import com.flowboard.comment_service.client.NotificationClient;
import com.flowboard.comment_service.dto.BulkNotificationRequestDto;
import com.flowboard.comment_service.dto.NotificationResponseDto;
import com.flowboard.comment_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class NotificationFallback implements NotificationClient {
    @Override
    public ResponseEntity<List<NotificationResponseDto>> sendBulk(BulkNotificationRequestDto dto) {
        log.error("CIRCUIT BREAKER - Notification service unreachable");
        throw new ServiceUnavailableException("Notification service not available");
    }
}
