package com.flowboard.comment_service.service;

public interface NotificationService {
    void sendNotification(Integer cardId, String content, Integer currentUserId);
}
