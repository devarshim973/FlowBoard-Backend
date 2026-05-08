package com.flowboard.notification_service.repository;

import com.flowboard.notification_service.entity.Notification;
import com.flowboard.notification_service.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Page<Notification> findByRecipientId(Integer recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndIsRead(Integer recipientId, boolean isRead);

    long countByRecipientIdAndIsRead(Integer recipientId, boolean isRead);

    List<Notification> findByNotificationType(NotificationType notificationType);

    List<Notification> findByRelatedId(Integer relatedId);

    void deleteByNotificationId(Integer notificationId);

    void deleteByRecipientIdAndIsRead(Integer recipientId, boolean isRead);
}
