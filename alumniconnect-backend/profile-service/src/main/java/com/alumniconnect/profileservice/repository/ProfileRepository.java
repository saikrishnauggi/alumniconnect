package com.alumniconnect.profileservice.repository;

import com.alumniconnect.profileservice.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    List<Profile> findByRole(String role);

    List<Profile> findByRoleAndIsAvailableTrue(String role);

    @Query("SELECT p FROM Profile p WHERE p.role = 'ALUMNI' AND " +
           "(LOWER(p.skills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.company) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.department) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Profile> searchAlumni(@Param("keyword") String keyword);
}
