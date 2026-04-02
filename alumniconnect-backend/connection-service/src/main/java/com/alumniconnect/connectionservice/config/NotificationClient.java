package com.alumniconnect.connectionservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;

    public NotificationClient(
            @Value("${notification.service.url}") String notificationServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.notificationServiceUrl = notificationServiceUrl;
    }

    public void sendNotification(Long userId, String type, String message) {
        try {
            Map<String, Object> payload = Map.of(
                "userId",  userId,
                "type",    type,
                "message", message
            );
            restTemplate.postForEntity(
                notificationServiceUrl + "/notifications",
                payload,
                Void.class
            );
        } catch (Exception e) {
            // Notification failure must NOT break connection flow
            log.warn("Could not send notification to userId {}: {}", userId, e.getMessage());
        }
    }
}
