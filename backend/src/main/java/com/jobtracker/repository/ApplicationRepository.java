package com.jobtracker.repository;

import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Application entities.
 *
 * IMPORTANT PATTERN — Per-user data isolation:
 *
 * Every query method includes userId so users can only access their own data.
 * This is our first line of defense (the service layer is the second).
 *
 * PAGINATION:
 *
 * Methods returning Page<Application> accept a Pageable parameter.
 * Spring Data generates LIMIT/OFFSET SQL and also counts total rows,
 * so the frontend can show "Page 1 of 5".
 *
 * Usage: applicationRepository.findByUserId(userId, PageRequest.of(0, 20))
 *   → Returns the first 20 applications for this user.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Get all applications for a user (paginated).
    // Spring Data translates this to:
    //   SELECT * FROM applications WHERE user_id = ? LIMIT ? OFFSET ?
    Page<Application> findByUserId(Long userId, Pageable pageable);

    // Find a specific application, but only if it belongs to this user.
    // Prevents user A from accessing user B's application by guessing the ID.
    Optional<Application> findByIdAndUserId(Long id, Long userId);

    // Filter by status (e.g., "show me all my INTERVIEW-stage applications")
    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    // Count applications per status — used by the dashboard.
    long countByUserIdAndStatus(Long userId, ApplicationStatus status);

    // Total applications for a user — used by analytics.
    long countByUserId(Long userId);
}
