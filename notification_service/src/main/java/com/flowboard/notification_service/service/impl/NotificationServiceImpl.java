package com.flowboard.notification_service.service.impl;

import com.flowboard.notification_service.Mapper.Mapper;
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
    private final Mapper<NotificationRequestDto, Notification> notificationRequestMapper;
    private final Mapper<Notification, NotificationResponseDto> notificationResponseMapper;
    private final EmailService emailService;
    private final UserClient userClient;

    private Notification getNotificationById(Integer id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification with notification id " + id + " not found"));
    }
    @Override
    public NotificationResponseDto send(NotificationRequestDto notificationRequestDto) {
        Notification notification = notificationRequestMapper.mapTo(notificationRequestDto);

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

        return notificationResponseMapper.mapTo(savedNotification);
    }

    @Override
    public void deleteNotification(Integer id) {
        Notification notification = getNotificationById(id);
        notificationRepository.delete(notification);
    }

    @Override
    public void markAsRead(Integer id) {
        Notification notification = getNotificationById(id);
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void deleteRead(Integer recipientId) {
        List<Notification> recipientReadNotifications =
                notificationRepository.findByRecipientIdAndIsRead(recipientId, true);
        for(Notification notification : recipientReadNotifications) {
            notificationRepository.delete(notification);
        }
    }

    @Override
    public Long getUnreadCount(Integer recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public List<NotificationResponseDto> sendBulk(BulkNotificationRequestDto bulkNotificationRequestDto) {
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
        return response;
    }

    @Override
    public CustomPageResponse<NotificationResponseDto> getByRecipientId(Integer recipientId, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(sortBy);
        if(direction.equals("asc")) sort.ascending();
        else sort.descending();

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
        List<Notification> recipientNotifications =
                notificationRepository.findByRecipientIdAndIsRead(recipientId, false);
        log.info(recipientNotifications.toString());
        for(Notification notification : recipientNotifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
}
