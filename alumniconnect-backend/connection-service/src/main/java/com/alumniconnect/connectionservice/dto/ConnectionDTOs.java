package com.alumniconnect.connectionservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

public class ConnectionDTOs {

    @Data
    public static class SendRequestDTO {
        @NotNull(message = "Alumni ID is required")
        private Long alumniId;
        private String message;
    }

    @Data
    public static class ConnectionResponse {
        private Long id;
        private Long studentId;
        private Long alumniId;
        private String status;
        private String message;
        private LocalDateTime requestedAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class StatusUpdateDTO {
        @NotNull(message = "Status is required")
        private String status;   // ACCEPTED or DECLINED
    }
}
