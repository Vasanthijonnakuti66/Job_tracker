
package com.jobtracker.repository;

import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Application entities.
 *
 * Every query includes userId where required
 * to maintain per-user data isolation.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Get all applications for a user (paginated).
    Page<Application> findByUserId(Long userId, Pageable pageable);

    // Search applications with optional filters.
    @Query("""
        SELECT a FROM Application a
        WHERE a.user.id = :userId
          AND (
              COALESCE(:company, '') = ''
              OR LOWER(a.companyName) LIKE
                 CONCAT('%', LOWER(CAST(:company AS string)), '%')
          )
          AND (
              COALESCE(:role, '') = ''
              OR LOWER(a.role) LIKE
                 CONCAT('%', LOWER(CAST(:role AS string)), '%')
          )
          AND (:status IS NULL OR a.status = :status)
          AND (:fromDate IS NULL OR a.dateApplied >= :fromDate)
          AND (:toDate IS NULL OR a.dateApplied <= :toDate)
        """)
    Page<Application> searchByUser(
            @Param("userId") Long userId,
            @Param("company") String company,
            @Param("role") String role,
            @Param("status") ApplicationStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    // Find an application only if it belongs to the specified user.
    Optional<Application> findByIdAndUserId(Long id, Long userId);

    // Filter applications by status.
    List<Application> findByUserIdAndStatus(
            Long userId,
            ApplicationStatus status
    );

    // Count applications per status.
    long countByUserIdAndStatus(Long userId, ApplicationStatus status);

    // Count all applications for a user.
    long countByUserId(Long userId);
}
