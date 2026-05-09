package com.flowboard.notification_service;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.service.NotificationService;
import com.flowboard.notification_service.service.impl.NotificationMessageListenerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationMessageListenerImplTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationMessageListenerImpl listener;

    @Test
    void processSingleNotification_delegatesToService() {
        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setRecipientId(1);

        listener.processSingleNotification(dto);

        verify(notificationService).send(dto);
    }

    @Test
    void processBulkNotification_delegatesToService() {
        BulkNotificationRequestDto dto = new BulkNotificationRequestDto();
        dto.setRecipientIds(List.of(1, 2));

        listener.processBulkNotification(dto);

        verify(notificationService).sendBulk(dto);
    }
}
