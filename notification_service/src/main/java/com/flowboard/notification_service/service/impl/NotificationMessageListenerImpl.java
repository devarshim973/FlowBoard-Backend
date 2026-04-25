package com.flowboard.notification_service.service.impl;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.service.NotificationMessageListener;
import com.flowboard.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/*
This will listen all the messages and call the notification service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageListenerImpl implements NotificationMessageListener {
    private final NotificationService notificationService;

    @Override
    @RabbitListener(queues = "single-notification-queue")
    public void processSingleNotification(NotificationRequestDto notificationRequestDto) {
        log.info("Single notification message received for recipient {}", notificationRequestDto.getRecipientId());
        notificationService.send(notificationRequestDto);
    }

    @Override
    public void processBulkNotification(BulkNotificationRequestDto bulkNotificationRequestDto) {
        log.info("Bulk notification message received for {} recipients", bulkNotificationRequestDto.getRecipientIds().size());
        notificationService.sendBulk(bulkNotificationRequestDto);
    }
}
