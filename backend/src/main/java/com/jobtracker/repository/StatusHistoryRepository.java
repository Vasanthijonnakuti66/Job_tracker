package com.jobtracker.repository;

import com.jobtracker.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for StatusHistory entities.
 *
 * DERIVED QUERY — Chaining field names:
 *
 *   findByApplicationIdOrderByChangedAtDesc
 *   ^^^^^^          ^^^^^^^^^^^^^^^^
 *   action          field + sort order
 *
 *   → SELECT * FROM status_history
 *     WHERE application_id = ?
 *     ORDER BY changed_at DESC
 *
 * The "Desc" suffix tells Spring Data to sort in descending order,
 * so the most recent status change appears first.
 */
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    // Full timeline for one application, newest first
    List<StatusHistory> findByApplicationIdOrderByChangedAtDesc(Long applicationId);

    // All history entries for an application (no specific order)
    List<StatusHistory> findByApplicationId(Long applicationId);
}
