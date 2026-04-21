package com.flowboard.notification_service.controller;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.service.NotificationService;
import com.flowboard.notification_service.utils.AppConstants;
import com.flowboard.notification_service.utils.CustomPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDto> handleSendNotification(@Valid @RequestBody NotificationRequestDto notificationRequestDto) {
        return ResponseEntity.accepted().body(notificationService.send(notificationRequestDto));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<NotificationResponseDto>> handleSendBulkNotification(
            @Valid @RequestBody BulkNotificationRequestDto dto) {
        return ResponseEntity.ok(notificationService.sendBulk(dto));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<String> handleMarkAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.accepted().body("Notification with id " + id + " marked as read");
    }

    @PutMapping("/readAll/{recipientId}")
    public ResponseEntity<String> handleMarkAllRead(@PathVariable Integer recipientId) {
        notificationService.markAllRead(recipientId);
        return ResponseEntity.accepted().body("Notifications marked as read");
    }

    @DeleteMapping("/delete-read/{recipientId}")
    public ResponseEntity<String> handleDeleteRead(@PathVariable Integer recipientId) {
        notificationService.deleteRead(recipientId);
        return ResponseEntity.accepted().body("Read notifications deleted");
    }

    @GetMapping("/recipient/{id}")
    public ResponseEntity<CustomPageResponse<NotificationResponseDto>> handleFindByRecipientId(@PathVariable Integer id,
                                                                               @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                               @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                               @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                               @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(notificationService.getByRecipientId(id, page, size, sortBy, direction));
    }

    @GetMapping("/recipient/unread-count/{recipientId}")
    public ResponseEntity<Long> handleGetUnreadCount(@PathVariable Integer recipientId) {
        return ResponseEntity.ok().body(notificationService.getUnreadCount(recipientId));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> handleDeleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.accepted().body("Notification with id " + id + " deleted successfully");
    }
}
