package com.jobtracker.repository;

import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * PostgreSQL aggregate queries used by the analytics page.
 */
public interface AnalyticsRepository extends Repository<Application, Long> {

    interface MonthlyApplicationsRow {
        LocalDate getMonth();

        Long getApplicationCount();
    }

    interface ApplicationsReachedStageRow {
        String getStatus();

        Long getApplicationCount();
    }

    interface AverageDaysPerStageRow {
        String getStatus();

        BigDecimal getAverageDays();
    }

    @Query(value = """
            SELECT CAST(DATE_TRUNC('month', date_applied) AS DATE) AS month,
                   COUNT(*) AS "applicationCount"
            FROM applications
            WHERE user_id = :userId
            GROUP BY DATE_TRUNC('month', date_applied)
            ORDER BY DATE_TRUNC('month', date_applied)
            """, nativeQuery = true)
    List<MonthlyApplicationsRow> findApplicationsPerMonth(@Param("userId") Long userId);

    @Query("""
            SELECT h.status AS status, COUNT(DISTINCT h.application.id) AS applicationCount
            FROM StatusHistory h
            WHERE h.application.user.id = :userId
            GROUP BY h.status
            """)
    List<ApplicationsReachedStageRow> countApplicationsReachedEachStage(
            @Param("userId") Long userId);

    @Query("""
            SELECT COUNT(DISTINCT source.application.id)
            FROM StatusHistory source
            WHERE source.application.user.id = :userId
              AND source.status = :fromStage
              AND EXISTS (
                  SELECT target.id
                  FROM StatusHistory target
                  WHERE target.application = source.application
                    AND target.status = :toStage
                    AND (target.changedAt > source.changedAt
                         OR (target.changedAt = source.changedAt AND target.id > source.id))
              )
            """)
    long countApplicationsConvertedBetweenStages(
            @Param("userId") Long userId,
            @Param("fromStage") ApplicationStatus fromStage,
            @Param("toStage") ApplicationStatus toStage
    );

    @Query(value = """
            WITH ordered_history AS (
                SELECT h.status,
                       h.changed_at,
                       LEAD(h.changed_at) OVER (
                           PARTITION BY h.application_id
                           ORDER BY h.changed_at, h.id
                       ) AS next_changed_at
                FROM status_history h
                JOIN applications a ON a.id = h.application_id
                WHERE a.user_id = :userId
            )
            SELECT status AS status,
                   CAST(AVG(
                       EXTRACT(EPOCH FROM (COALESCE(next_changed_at, CURRENT_TIMESTAMP) - changed_at))
                       / 86400.0
                   ) AS NUMERIC(12, 2)) AS "averageDays"
            FROM ordered_history
            GROUP BY status
            ORDER BY status
            """, nativeQuery = true)
    List<AverageDaysPerStageRow> findAverageDaysPerStage(@Param("userId") Long userId);
}
