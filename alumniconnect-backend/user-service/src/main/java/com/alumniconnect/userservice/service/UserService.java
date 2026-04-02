package com.alumniconnect.userservice.service;

import com.alumniconnect.userservice.dto.AuthDTOs.*;
import com.alumniconnect.userservice.model.User;
import com.alumniconnect.userservice.repository.UserRepository;
import com.alumniconnect.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Adding .trim() prevents crashes from accidental spaces in the JSON
        user.setRole(User.Role.valueOf(request.getRole().trim().toUpperCase()));

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, saved.getId(), saved.getEmail(), saved.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    public ValidateTokenResponse validateToken(String token) {
        if (!jwtUtil.isTokenValid(token)) {
            return new ValidateTokenResponse(null, null, null, false);
        }
        Long userId = jwtUtil.extractUserId(token);
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);
        return new ValidateTokenResponse(userId, email, role, true);
    }
}
