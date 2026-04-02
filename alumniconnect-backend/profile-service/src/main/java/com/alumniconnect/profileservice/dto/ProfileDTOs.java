package com.alumniconnect.profileservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class ProfileDTOs {

    @Data
    public static class CreateProfileRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;
        private String department;
        private String graduationYear;
        private String company;
        private String jobTitle;
        private String skills;
        private String bio;
        private String role;
        private Boolean isAvailable = true;
    }

    @Data
    public static class UpdateProfileRequest {
        private String fullName;
        private String department;
        private String graduationYear;
        private String company;
        private String jobTitle;
        private String skills;
        private String bio;
        private Boolean isAvailable;
    }

    @Data
    public static class ProfileResponse {
        private Long id;
        private Long userId;
        private String fullName;
        private String department;
        private String graduationYear;
        private String company;
        private String jobTitle;
        private String skills;
        private String bio;
        private String role;
        private Boolean isAvailable;
    }
}
