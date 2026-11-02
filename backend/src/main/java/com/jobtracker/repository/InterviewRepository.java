package com.jobtracker.repository;

import com.jobtracker.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT i FROM Interview i " +
           "WHERE i.id = :interviewId " +
           "AND i.application.id = :applicationId " +
           "AND i.application.user.id = :userId")
    Optional<Interview> findOwnedInterview(
            @Param("interviewId") Long interviewId,
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId
    );

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

    @Query("""
            SELECT i FROM Interview i
            JOIN FETCH i.application a
            JOIN FETCH a.user u
            WHERE i.reminderSentAt IS NULL
              AND (
                    i.interviewDate > :today
                    OR (i.interviewDate = :today
                        AND (i.interviewTime IS NULL OR i.interviewTime >= :currentTime))
                  )
              AND (
                    i.interviewDate < :tomorrow
                    OR (i.interviewDate = :tomorrow
                        AND (i.interviewTime IS NULL OR i.interviewTime <= :tomorrowTime))
                  )
            ORDER BY i.interviewDate, i.interviewTime
            """)
    List<Interview> findUnremindedWithinNextDay(
            @Param("today") LocalDate today,
            @Param("currentTime") LocalTime currentTime,
            @Param("tomorrow") LocalDate tomorrow,
            @Param("tomorrowTime") LocalTime tomorrowTime
    );
}
