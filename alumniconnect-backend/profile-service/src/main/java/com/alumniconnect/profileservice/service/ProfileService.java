package com.alumniconnect.profileservice.service;

import com.alumniconnect.profileservice.dto.ProfileDTOs.*;
import com.alumniconnect.profileservice.model.Profile;
import com.alumniconnect.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 👈 Add this

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // 👈 Ensures DB commits are handled correctly
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileResponse createProfile(Long userId, CreateProfileRequest request) {
        // Double check for existing profile to prevent Unique Constraint violations
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Profile already exists for user ID: " + userId);
        }

        Profile profile = new Profile();
        profile.setUserId(userId);
        profile.setFullName(request.getFullName());
        profile.setDepartment(request.getDepartment());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setCompany(request.getCompany());
        profile.setJobTitle(request.getJobTitle());
        profile.setSkills(request.getSkills());
        profile.setBio(request.getBio());
        profile.setRole(request.getRole());
        profile.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        Profile savedProfile = profileRepository.save(profile);
        return toResponse(savedProfile);
    }

    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
        if (request.getGraduationYear() != null) profile.setGraduationYear(request.getGraduationYear());
        if (request.getCompany() != null) profile.setCompany(request.getCompany());
        if (request.getJobTitle() != null) profile.setJobTitle(request.getJobTitle());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getIsAvailable() != null) profile.setIsAvailable(request.getIsAvailable());

        return toResponse(profileRepository.save(profile));
    }

    public ProfileResponse getProfileByUserId(Long userId) {
        return toResponse(profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found")));
    }

    public ProfileResponse getProfileById(Long profileId) {
        return toResponse(profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found")));
    }

    public List<ProfileResponse> getAllAlumni() {
        return profileRepository.findByRole("ALUMNI")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProfileResponse> getAvailableAlumni() {
        return profileRepository.findByRoleAndIsAvailableTrue("ALUMNI")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProfileResponse> searchAlumni(String keyword) {
        return profileRepository.searchAlumni(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ProfileResponse toResponse(Profile p) {
        ProfileResponse r = new ProfileResponse();
        r.setId(p.getId());
        r.setUserId(p.getUserId());
        r.setFullName(p.getFullName());
        r.setDepartment(p.getDepartment());
        r.setGraduationYear(p.getGraduationYear());
        r.setCompany(p.getCompany());
        r.setJobTitle(p.getJobTitle());
        r.setSkills(p.getSkills());
        r.setBio(p.getBio());
        r.setRole(p.getRole());
        r.setIsAvailable(p.getIsAvailable());
        return r;
    }
}
