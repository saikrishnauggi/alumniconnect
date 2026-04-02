package com.alumniconnect.profileservice.controller;

import com.alumniconnect.profileservice.dto.ProfileDTOs.*;
import com.alumniconnect.profileservice.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // Create profile — userId comes from Gateway header (set from JWT)
//    @PostMapping
//    public ResponseEntity<ProfileResponse> create(
//            @RequestHeader("X-User-Id") Long userId,
//            @RequestHeader("X-User-Role") String role,
//            @Valid @RequestBody CreateProfileRequest request) {
//        request.setRole(role);
//        return ResponseEntity.ok(profileService.createProfile(userId, request));
//    }
    @PostMapping
    public ResponseEntity<ProfileResponse> create(
            @RequestHeader("X-User-Id") String userIdStr,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateProfileRequest request) {

        try {
            Long userId = Long.parseLong(userIdStr);
            request.setRole(role);
            return ResponseEntity.ok(profileService.createProfile(userId, request));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid User ID format in header");
        }
    }

    // Update own profile
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> update(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    // Get own profile
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    // Get profile by profileId (public)
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    // Get profile by userId (used by other services)
    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId));
    }

    // List all alumni
    @GetMapping("/alumni")
    public ResponseEntity<List<ProfileResponse>> getAllAlumni() {
        return ResponseEntity.ok(profileService.getAllAlumni());
    }

    // List available alumni only
    @GetMapping("/alumni/available")
    public ResponseEntity<List<ProfileResponse>> getAvailableAlumni() {
        return ResponseEntity.ok(profileService.getAvailableAlumni());
    }

    // Search alumni by keyword
    @GetMapping("/alumni/search")
    public ResponseEntity<List<ProfileResponse>> searchAlumni(
            @RequestParam String keyword) {
        return ResponseEntity.ok(profileService.searchAlumni(keyword));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Profile Service is running");
    }
}
