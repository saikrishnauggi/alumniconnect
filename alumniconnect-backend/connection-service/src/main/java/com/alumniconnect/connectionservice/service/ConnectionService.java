package com.alumniconnect.connectionservice.service;

import com.alumniconnect.connectionservice.config.NotificationClient;
import com.alumniconnect.connectionservice.dto.ConnectionDTOs.*;
import com.alumniconnect.connectionservice.model.Connection;
import com.alumniconnect.connectionservice.repository.ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final NotificationClient notificationClient;

    public ConnectionResponse sendRequest(Long studentId, SendRequestDTO dto) {
        // Prevent duplicate pending requests
        if (connectionRepository.existsByStudentIdAndAlumniIdAndStatus(
                studentId, dto.getAlumniId(), Connection.Status.PENDING)) {
            throw new RuntimeException("A pending request already exists for this alumni");
        }

        Connection connection = new Connection();
        connection.setStudentId(studentId);
        connection.setAlumniId(dto.getAlumniId());
        connection.setMessage(dto.getMessage());
        connection.setStatus(Connection.Status.PENDING);

        Connection saved = connectionRepository.save(connection);

        // Notify the alumni
        notificationClient.sendNotification(
            dto.getAlumniId(),
            "NEW_REQUEST",
            "You have a new mentorship request from a student."
        );

        return toResponse(saved);
    }

    public ConnectionResponse updateStatus(Long connectionId, Long alumniId, StatusUpdateDTO dto) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        if (!connection.getAlumniId().equals(alumniId)) {
            throw new RuntimeException("Unauthorized: You can only update your own requests");
        }

        Connection.Status newStatus = Connection.Status.valueOf(dto.getStatus().toUpperCase());
        connection.setStatus(newStatus);
        Connection updated = connectionRepository.save(connection);

        // Notify the student of the outcome
        String notifMessage = newStatus == Connection.Status.ACCEPTED
            ? "Your mentorship request was accepted!"
            : "Your mentorship request was declined.";

        notificationClient.sendNotification(connection.getStudentId(), newStatus.name(), notifMessage);

        return toResponse(updated);
    }

    public List<ConnectionResponse> getByStudent(Long studentId) {
        return connectionRepository.findByStudentId(studentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ConnectionResponse> getByAlumni(Long alumniId) {
        return connectionRepository.findByAlumniId(alumniId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ConnectionResponse> getPendingForAlumni(Long alumniId) {
        return connectionRepository.findByAlumniIdAndStatus(alumniId, Connection.Status.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ConnectionResponse> getAcceptedForStudent(Long studentId) {
        return connectionRepository.findByStudentIdAndStatus(studentId, Connection.Status.ACCEPTED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ConnectionResponse toResponse(Connection c) {
        ConnectionResponse r = new ConnectionResponse();
        r.setId(c.getId());
        r.setStudentId(c.getStudentId());
        r.setAlumniId(c.getAlumniId());
        r.setStatus(c.getStatus().name());
        r.setMessage(c.getMessage());
        r.setRequestedAt(c.getRequestedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
