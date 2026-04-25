package com.flowboard.card_service.fallback;

import com.flowboard.card_service.client.NotificationClient;
import com.flowboard.card_service.dto.BulkNotificationRequestDto;
import com.flowboard.card_service.dto.NotificationRequestDto;
import com.flowboard.card_service.dto.NotificationResponseDto;
import com.flowboard.card_service.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class NotificationFallback implements NotificationClient {
    @Override
    public ResponseEntity<NotificationResponseDto> handleSendNotification(NotificationRequestDto notificationRequestDto) {
        log.error("CIRCUIT BREAKER - Notification service unreachable");
        throw new ServiceUnavailableException("Notification service not available");
    }

    @Override
    public ResponseEntity<List<NotificationResponseDto>> handleSendBulkNotification(BulkNotificationRequestDto dto) {
        log.error("CIRCUIT BREAKER - Notification service unreachable");
        throw new ServiceUnavailableException("Notification service not available");
    }
}
