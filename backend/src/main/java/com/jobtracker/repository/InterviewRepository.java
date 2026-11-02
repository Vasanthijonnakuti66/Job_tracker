package com.jobtracker.repository;

import com.jobtracker.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Interview entities.
 *
 * CUSTOM JPQL QUERY (@Query):
 *
 * When derived method names get too long or complex, you can write
 * JPQL (Java Persistence Query Language) directly. JPQL looks like SQL
 * but operates on ENTITIES and FIELDS, not tables and columns:
 *
 *   "SELECT i FROM Interview i"  ← Interview is the Java class, not a table
 *   "i.application.user.id"      ← navigates the entity relationships
 *
 * Spring Data replaces :paramName with the method parameter value.
 */
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    // All interviews for a specific application
    List<Interview> findByApplicationIdOrderByInterviewDateAsc(Long applicationId);

    // Upcoming interviews for a user within a date range.
    // This powers the "Interviews this week" dashboard widget.
    // We traverse: Interview → Application → User to filter by user.
    @Query("SELECT i FROM Interview i " +
           "WHERE i.application.user.id = :userId " +
           "AND i.interviewDate BETWEEN :startDate AND :endDate " +
           "ORDER BY i.interviewDate ASC, i.interviewTime ASC")
    List<Interview> findUpcomingByUserId(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
