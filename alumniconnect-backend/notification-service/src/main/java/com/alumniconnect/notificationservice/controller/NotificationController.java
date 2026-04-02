package com.alumniconnect.notificationservice.controller;

import com.alumniconnect.notificationservice.dto.NotificationDTOs.*;
import com.alumniconnect.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // Called internally by connection-service (no auth header needed)
    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.create(request));
    }

    // Get all notifications for logged-in user
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getAllForUser(userId));
    }

    // Get only unread
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadForUser(userId));
    }

    // Unread count (for badge on bell icon)
    @GetMapping("/unread/count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    // Mark single notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    // Mark all as read
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
