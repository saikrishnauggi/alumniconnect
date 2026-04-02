package com.alumniconnect.connectionservice.repository;

import com.alumniconnect.connectionservice.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByStudentId(Long studentId);

    List<Connection> findByAlumniId(Long alumniId);

    List<Connection> findByAlumniIdAndStatus(Long alumniId, Connection.Status status);

    List<Connection> findByStudentIdAndStatus(Long studentId, Connection.Status status);

    Optional<Connection> findByStudentIdAndAlumniId(Long studentId, Long alumniId);

    boolean existsByStudentIdAndAlumniIdAndStatus(Long studentId, Long alumniId, Connection.Status status);
}
