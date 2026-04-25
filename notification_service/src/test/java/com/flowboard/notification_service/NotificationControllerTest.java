package com.flowboard.notification_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.notification_service.controller.NotificationController;
import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.entity.NotificationType;
import com.flowboard.notification_service.entity.RelatedType;
import com.flowboard.notification_service.service.NotificationService;
import com.flowboard.notification_service.utils.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomPageResponse<NotificationResponseDto> getPage() {

        PageImpl<NotificationResponseDto> page =
                new PageImpl<>(
                        List.of(new NotificationResponseDto()),
                        PageRequest.of(0, 5),
                        1
                );

        return new CustomPageResponse<>(page);
    }

    private NotificationRequestDto getNotificationRequest() {

        return NotificationRequestDto.builder()
                .recipientId(1)
                .actorId(2)
                .notificationType(NotificationType.COMMENT)
                .title("Task Assigned")
                .message("You have been assigned")
                .relatedId(10)
                .relatedType(RelatedType.CARD)
                .build();
    }

    private BulkNotificationRequestDto getBulkRequest() {

        BulkNotificationRequestDto dto =
                new BulkNotificationRequestDto();

        dto.setRecipientIds(List.of(1, 2));
        dto.setActorId(2);
        dto.setTitle("Bulk");
        dto.setMessage("Bulk message");
        dto.setRelatedId(10);
        dto.setNotificationType(NotificationType.COMMENT);
        dto.setRelatedType(RelatedType.CARD);

        return dto;
    }

    @Test
    void handleSendNotification_positive() throws Exception {

        NotificationResponseDto response =
                new NotificationResponseDto();
        response.setNotificationId(1);

        when(notificationService.send(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                getNotificationRequest()
                        )))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.notificationId").value(1));
    }

    @Test
    void handleSendNotification_negative() throws Exception {

        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleSendBulkNotification_positive() throws Exception {

        when(notificationService.sendBulk(any()))
                .thenReturn(List.of(new NotificationResponseDto()));

        mockMvc.perform(post("/api/v1/notifications/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                getBulkRequest()
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void handleSendBulkNotification_negative() throws Exception {

        mockMvc.perform(post("/api/v1/notifications/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleMarkAsRead_positive() throws Exception {

        doNothing().when(notificationService)
                .markAsRead(1);

        mockMvc.perform(put("/api/v1/notifications/read/1"))
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .string("Notification with id 1 marked as read"));
    }

    @Test
    void handleMarkAsRead_negative() throws Exception {

        doThrow(new RuntimeException())
                .when(notificationService)
                .markAsRead(99);

        mockMvc.perform(put("/api/v1/notifications/read/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleMarkAllRead_positive() throws Exception {

        doNothing().when(notificationService)
                .markAllRead(1);

        mockMvc.perform(put("/api/v1/notifications/readAll/1"))
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .string("Notifications marked as read"));
    }

    @Test
    void handleDeleteRead_positive() throws Exception {

        doNothing().when(notificationService)
                .deleteRead(1);

        mockMvc.perform(delete("/api/v1/notifications/delete-read/1"))
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .string("Read notifications deleted"));
    }

    @Test
    void handleFindByRecipientId_positive() throws Exception {

        when(notificationService.getByRecipientId(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/notifications/recipient/1"))
                .andExpect(status().isOk());
    }

    @Test
    void handleGetUnreadCount_positive() throws Exception {

        when(notificationService.getUnreadCount(1))
                .thenReturn(5L);

        mockMvc.perform(get("/api/v1/notifications/recipient/unread-count/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void handleDeleteNotification_positive() throws Exception {

        doNothing().when(notificationService)
                .deleteNotification(1);

        mockMvc.perform(delete("/api/v1/notifications/delete/1"))
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .string("Notification with id 1 deleted successfully"));
    }

    @Test
    void handleDeleteNotification_negative() throws Exception {

        doThrow(new RuntimeException())
                .when(notificationService)
                .deleteNotification(99);

        mockMvc.perform(delete("/api/v1/notifications/delete/99"))
                .andExpect(status().isBadRequest());
    }
}