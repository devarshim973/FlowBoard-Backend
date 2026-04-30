package com.flowboard.card_service.service;

import com.flowboard.card_service.dto.BulkNotificationRequestDto;
import com.flowboard.card_service.dto.NotificationRequestDto;

public interface NotificationProcedure {
    void sendBulk(BulkNotificationRequestDto message);

    void sendSingle(NotificationRequestDto message);
}
