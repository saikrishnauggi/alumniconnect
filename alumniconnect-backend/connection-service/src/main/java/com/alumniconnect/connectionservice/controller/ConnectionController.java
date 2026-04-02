package com.alumniconnect.connectionservice.controller;

import com.alumniconnect.connectionservice.dto.ConnectionDTOs.*;
import com.alumniconnect.connectionservice.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    // Student sends a mentorship request
    @PostMapping("/request")
    public ResponseEntity<ConnectionResponse> sendRequest(
            @RequestHeader("X-User-Id") Long studentId,
            @Valid @RequestBody SendRequestDTO dto) {
        return ResponseEntity.ok(connectionService.sendRequest(studentId, dto));
    }

    // Alumni accepts or declines
    @PutMapping("/{id}/status")
    public ResponseEntity<ConnectionResponse> updateStatus(
            @RequestHeader("X-User-Id") Long alumniId,
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateDTO dto) {
        return ResponseEntity.ok(connectionService.updateStatus(id, alumniId, dto));
    }

    // Student views all their requests
    @GetMapping("/my-requests")
    public ResponseEntity<List<ConnectionResponse>> myRequests(
            @RequestHeader("X-User-Id") Long studentId) {
        return ResponseEntity.ok(connectionService.getByStudent(studentId));
    }

    // Student views only accepted connections
    @GetMapping("/my-connections")
    public ResponseEntity<List<ConnectionResponse>> myConnections(
            @RequestHeader("X-User-Id") Long studentId) {
        return ResponseEntity.ok(connectionService.getAcceptedForStudent(studentId));
    }

    // Alumni views all incoming requests
    @GetMapping("/incoming")
    public ResponseEntity<List<ConnectionResponse>> incoming(
            @RequestHeader("X-User-Id") Long alumniId) {
        return ResponseEntity.ok(connectionService.getByAlumni(alumniId));
    }

    // Alumni views only pending requests
    @GetMapping("/incoming/pending")
    public ResponseEntity<List<ConnectionResponse>> pending(
            @RequestHeader("X-User-Id") Long alumniId) {
        return ResponseEntity.ok(connectionService.getPendingForAlumni(alumniId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Connection Service is running");
    }
}
