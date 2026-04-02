package com.alumniconnect.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class AuthDTOs {

    @Data
    public static class RegisterRequest {
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        @NotNull(message = "Role is required (STUDENT or ALUMNI)")
        private String role;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private Long userId;
        private String email;
        private String role;

        public AuthResponse(String token, Long userId, String email, String role) {
            this.token = token;
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
    }

    @Data
    public static class ValidateTokenResponse {
        private Long userId;
        private String email;
        private String role;
        private boolean valid;

        public ValidateTokenResponse(Long userId, String email, String role, boolean valid) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.valid = valid;
        }
    }
}