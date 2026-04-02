package com.alumniconnect.profileservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String department;

    @Column(name = "graduation_year")
    private String graduationYear;

    private String company;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(length = 500)
    private String skills;   // comma-separated, e.g. "Java,Spring,MySQL"

    @Column(length = 1000)
    private String bio;

    private String role;     // STUDENT or ALUMNI (copied from user-service)

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}
