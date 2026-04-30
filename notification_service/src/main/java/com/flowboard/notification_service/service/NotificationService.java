package com.flowboard.notification_service.service;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.utils.CustomPageResponse;

import java.util.List;

public interface NotificationService {
    public NotificationResponseDto send(NotificationRequestDto notificationRequestDto);

    void deleteNotification(Integer id);

    void markAsRead(Integer id);

    void deleteRead(Integer recipientId);

    Long getUnreadCount(Integer recipientId);

    List<NotificationResponseDto> sendBulk(BulkNotificationRequestDto dto);

    CustomPageResponse<NotificationResponseDto> getByRecipientId(Integer id, int page, int size, String sortBy, String direction);

    void markAllRead(Integer recipientId);
}
