package com.alumniconnect.notificationservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class NotificationDTOs {

    @Data
    public static class CreateNotificationRequest {
        private Long userId;
        private String type;
        private String message;
    }

    @Data
    public static class NotificationResponse {
        private Long id;
        private Long userId;
        private String type;
        private String message;
        private Boolean isRead;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UnreadCountResponse {
        private Long userId;
        private long count;

        public UnreadCountResponse(Long userId, long count) {
            this.userId = userId;
            this.count  = count;
        }
    }
}
