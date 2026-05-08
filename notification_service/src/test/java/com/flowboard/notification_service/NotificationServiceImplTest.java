package com.flowboard.notification_service;

import com.flowboard.notification_service.Mapper.Mapper;
import com.flowboard.notification_service.Mapper.impl.NotificationRequestDtoMapper;
import com.flowboard.notification_service.Mapper.impl.NotificationResponseDtoMapper;
import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.entity.Notification;
import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
import com.flowboard.notification_service.exception.NotificationNotFoundException;
import com.flowboard.notification_service.repository.NotificationRepository;
import com.flowboard.notification_service.service.impl.NotificationServiceImpl;
import com.flowboard.notification_service.utils.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationRequestDtoMapper requestMapper;

    @Mock
    private NotificationResponseDtoMapper responseMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void send_positive() {

        NotificationRequestDto dto =
                NotificationRequestDto.builder()
                        .recipientId(1)
                        .actorId(2)
                        .notificationType(NotificationType.COMMENT)
                        .title("Title")
                        .message("Message")
                        .relatedId(10)
                        .relatedType(RelatedType.CARD)
                        .build();

        Notification notification = new Notification();

        when(requestMapper.mapTo(any(NotificationRequestDto.class)))
                .thenReturn(notification);

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        when(responseMapper.mapTo(any(Notification.class)))
                .thenReturn(new NotificationResponseDto());

        notificationService.send(dto);
    }

    @Test
    void sendBulk_positive() {

        BulkNotificationRequestDto dto =
                new BulkNotificationRequestDto();

        dto.setRecipientIds(List.of(1, 2));
        dto.setActorId(2);
        dto.setTitle("Bulk");
        dto.setMessage("Hello");
        dto.setRelatedId(10);
        dto.setNotificationType(NotificationType.COMMENT);
        dto.setRelatedType(RelatedType.CARD);

        when(requestMapper.mapTo(any(NotificationRequestDto.class)))
                .thenReturn(new Notification());

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(new Notification());

        when(responseMapper.mapTo(any(Notification.class)))
                .thenReturn(new NotificationResponseDto());

        List<NotificationResponseDto> result =
                notificationService.sendBulk(dto);

        assertEquals(2, result.size());
    }

    @Test
    void markAsRead_positive() {

        Notification notification = new Notification();

        when(notificationRepository.findById(1))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(1);

        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_negative() {

        when(notificationRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.markAsRead(99));
    }

    @Test
    void markAllRead_positive() {

        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(notificationRepository.findByRecipientIdAndIsRead(1, false))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllRead(1);

        verify(notificationRepository).save(n1);
        verify(notificationRepository).save(n2);
    }

    @Test
    void deleteRead_positive() {

        when(notificationRepository.findByRecipientIdAndIsRead(1, true))
                .thenReturn(List.of(new Notification()));

        notificationService.deleteRead(1);

        verify(notificationRepository)
                .findByRecipientIdAndIsRead(1, true);
    }

    @Test
    void getByRecipientId_positive() {

        Notification notification = new Notification();

        PageImpl<Notification> page =
                new PageImpl<>(
                        List.of(notification),
                        PageRequest.of(0, 5),
                        1
                );

        when(notificationRepository.findByRecipientId(
                anyInt(),
                any()))
                .thenReturn(page);

        when(responseMapper.mapTo(any(Notification.class)))
                .thenReturn(new NotificationResponseDto());

        CustomPageResponse<NotificationResponseDto> result =
                notificationService.getByRecipientId(
                        1,
                        0,
                        5,
                        "notificationId",
                        "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getUnreadCount_positive() {

        when(notificationRepository
                .countByRecipientIdAndIsRead(1, false))
                .thenReturn(5L);

        Long result =
                notificationService.getUnreadCount(1);

        assertEquals(5L, result);
    }

    @Test
    void deleteNotification_positive() {

        Notification notification = new Notification();

        when(notificationRepository.findById(1))
                .thenReturn(Optional.of(notification));

        notificationService.deleteNotification(1);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_negative() {

        when(notificationRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class,
                () -> notificationService.deleteNotification(99));
    }
}