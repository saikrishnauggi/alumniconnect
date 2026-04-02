package com.alumniconnect.notificationservice.service;

import com.alumniconnect.notificationservice.dto.NotificationDTOs.*;
import com.alumniconnect.notificationservice.model.Notification;
import com.alumniconnect.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse create(CreateNotificationRequest request) {
        Notification n = new Notification();
        n.setUserId(request.getUserId());
        n.setType(request.getType());
        n.setMessage(request.getMessage());
        n.setIsRead(false);
        return toResponse(notificationRepository.save(n));
    }

    public List<NotificationResponse> getAllForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadForUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UnreadCountResponse getUnreadCount(Long userId) {
        return new UnreadCountResponse(userId,
                notificationRepository.countByUserIdAndIsReadFalse(userId));
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setUserId(n.getUserId());
        r.setType(n.getType());
        r.setMessage(n.getMessage());
        r.setIsRead(n.getIsRead());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
