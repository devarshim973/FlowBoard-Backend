package com.flowboard.notification_service.controller;

import com.flowboard.notification_service.dto.BulkNotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationRequestDto;
import com.flowboard.notification_service.dto.NotificationResponseDto;
import com.flowboard.notification_service.service.NotificationService;
import com.flowboard.notification_service.utils.AppConstants;
import com.flowboard.notification_service.utils.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification APIs", description = "APIs for managing notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Send a notification", description = "Send a single notification to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDto> handleSendNotification(@Valid @RequestBody NotificationRequestDto notificationRequestDto) {
        return ResponseEntity.accepted().body(notificationService.send(notificationRequestDto));
    }

    @Operation(summary = "Send bulk notifications", description = "Send notifications to multiple users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk notifications sent"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload")
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<NotificationResponseDto>> handleSendBulkNotification(
            @Valid @RequestBody BulkNotificationRequestDto dto) {
        return ResponseEntity.ok(notificationService.sendBulk(dto));
    }

    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Notification marked as read"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/read/{id}")
    public ResponseEntity<String> handleMarkAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return ResponseEntity.accepted().body("Notification with id " + id + " marked as read");
    }

    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications of a user as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "All notifications marked as read")
    })
    @PutMapping("/readAll/{recipientId}")
    public ResponseEntity<String> handleMarkAllRead(@PathVariable Integer recipientId) {
        notificationService.markAllRead(recipientId);
        return ResponseEntity.accepted().body("Notifications marked as read");
    }

    @Operation(summary = "Delete read notifications", description = "Delete all read notifications of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Read notifications deleted")
    })
    @DeleteMapping("/delete-read/{recipientId}")
    public ResponseEntity<String> handleDeleteRead(@PathVariable Integer recipientId) {
        notificationService.deleteRead(recipientId);
        return ResponseEntity.accepted().body("Read notifications deleted");
    }

    @Operation(summary = "Get notifications by recipient", description = "Fetch paginated notifications for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notifications fetched successfully")
    })
    @GetMapping("/recipient/{id}")
    public ResponseEntity<CustomPageResponse<NotificationResponseDto>> handleFindByRecipientId(@PathVariable Integer id,
                                                                               @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                               @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                               @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                               @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(notificationService.getByRecipientId(id, page, size, sortBy, direction));
    }

    @Operation(summary = "Get unread notification count", description = "Returns count of unread notifications for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread count fetched")
    })
    @GetMapping("/recipient/unread-count/{recipientId}")
    public ResponseEntity<Long> handleGetUnreadCount(@PathVariable Integer recipientId) {
        return ResponseEntity.ok().body(notificationService.getUnreadCount(recipientId));
    }

    @Operation(summary = "Delete a notification", description = "Delete a specific notification by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Notification deleted"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> handleDeleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.accepted().body("Notification with id " + id + " deleted successfully");
    }
}
