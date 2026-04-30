package com.flowboard.notification_service.service.impl;

import com.flowboard.notification_service.Mapper.Mapper;
import com.flowboard.notification_service.Mapper.impl.NotificationRequestDtoMapper;
import com.flowboard.notification_service.Mapper.impl.NotificationResponseDtoMapper;
import com.flowboard.notification_service.client.UserClient;
import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.entity.Notification;
import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.exception.NotificationNotFoundException;
import com.flowboard.notification_service.repository.NotificationRepository;
import com.flowboard.notification_service.service.EmailService;
import com.flowboard.notification_service.service.NotificationService;
import com.flowboard.notification_service.utils.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationRequestDtoMapper notificationRequestMapper;
    private final NotificationResponseDtoMapper notificationResponseMapper;
    private final EmailService emailService;
    private final UserClient userClient;

    private Notification getNotificationById(Integer id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification with notification id " + id + " not found"));
    }
    @Override
    public NotificationResponseDto send(NotificationRequestDto notificationRequestDto) {
        Notification notification = notificationRequestMapper.mapTo(notificationRequestDto);
        log.info("Send notification requested for recipient {}", notification.getRecipientId());

        // here notification service will call auth service to get the mail of user
        // by the given id
        if(notification.getNotificationType() == NotificationType.ASSIGNMENT) {
            String recipientEmail = userClient.getUserEmail(notification.getRecipientId());
            emailService.sendAssignmentEmail(recipientEmail, notification);
        }
        else if(notification.getNotificationType() == NotificationType.MENTION) {
            String recipientEmail = userClient.getUserEmail(notification.getRecipientId());
            emailService.sendMentionEmail(recipientEmail, notification);
        }
        else if(notification.getNotificationType() == NotificationType.DUE_DATE) {
            String recipientEmail = userClient.getUserEmail(notification.getRecipientId());
            emailService.sendDueDateEmail(recipientEmail, notification);
        }
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created with id {}", savedNotification.getNotificationId());

        return notificationResponseMapper.mapTo(savedNotification);
    }

    @Override
    public void deleteNotification(Integer id) {
        log.info("Delete notification requested for notification {}", id);
        Notification notification = getNotificationById(id);
        notificationRepository.delete(notification);
        log.info("Notification deleted with id {}", id);
    }

    @Override
    public void markAsRead(Integer id) {
        log.info("Mark notification as read requested for notification {}", id);
        Notification notification = getNotificationById(id);
        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Notification marked as read with id {}", id);
    }

    @Override
    public void deleteRead(Integer recipientId) {
        log.info("Delete read notifications requested for recipient {}", recipientId);
        List<Notification> recipientReadNotifications =
                notificationRepository.findByRecipientIdAndIsRead(recipientId, true);
        for(Notification notification : recipientReadNotifications) {
            notificationRepository.delete(notification);
        }
        log.info("Deleted read notifications for recipient {}", recipientId);
    }

    @Override
    public Long getUnreadCount(Integer recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public List<NotificationResponseDto> sendBulk(BulkNotificationRequestDto bulkNotificationRequestDto) {
        log.info("Bulk notification requested for {} recipients", bulkNotificationRequestDto.getRecipientIds().size());
        List<NotificationResponseDto> response = new ArrayList<>();
        NotificationRequestDto notificationRequestDto = NotificationRequestDto
                .builder()
                .title(bulkNotificationRequestDto.getTitle())
                .message(bulkNotificationRequestDto.getMessage())
                .actorId(bulkNotificationRequestDto.getActorId())
                .notificationType(bulkNotificationRequestDto.getNotificationType())
                .relatedType(bulkNotificationRequestDto.getRelatedType())
                .relatedId(bulkNotificationRequestDto.getRelatedId())
                .build();

        for(Integer recipientId : bulkNotificationRequestDto.getRecipientIds()) {
            notificationRequestDto.setRecipientId(recipientId);
            response.add(send(notificationRequestDto));
        }
        log.info("Bulk notification completed for {} recipients", bulkNotificationRequestDto.getRecipientIds().size());
        return response;
    }

    @Override
    public CustomPageResponse<NotificationResponseDto> getByRecipientId(Integer recipientId, int page, int size, String sortBy, String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Notification> notificationPage =
                notificationRepository.findByRecipientId(recipientId, pageable);

        Page<NotificationResponseDto> notificationResponsePage = notificationPage
                .map(notificationResponseMapper::mapTo);

        CustomPageResponse<NotificationResponseDto> customPageResponse =
                new CustomPageResponse<>(notificationResponsePage);

        return customPageResponse;
    }

    @Override
    public void markAllRead(Integer recipientId) {
        log.info("Mark all notifications as read requested for recipient {}", recipientId);
        List<Notification> recipientNotifications =
                notificationRepository.findByRecipientIdAndIsRead(recipientId, false);
        for(Notification notification : recipientNotifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        log.info("All unread notifications marked as read for recipient {}", recipientId);
    }
}
