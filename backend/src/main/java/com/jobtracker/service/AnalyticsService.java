package com.jobtracker.service;

import com.jobtracker.dto.AnalyticsResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final List<ApplicationStatus> PIPELINE_STAGES = List.of(
            ApplicationStatus.APPLIED,
            ApplicationStatus.ASSESSMENT,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.OFFER
    );

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public AnalyticsResponse getAnalytics(Long userId) {
        List<AnalyticsResponse.MonthlyApplications> monthly = analyticsRepository
                .findApplicationsPerMonth(userId)
                .stream()
                .map(row -> new AnalyticsResponse.MonthlyApplications(
                        row.getMonth(), row.getApplicationCount()))
                .toList();

        Map<ApplicationStatus, Long> reachedCounts = new EnumMap<>(ApplicationStatus.class);
        for (AnalyticsRepository.ApplicationsReachedStageRow row
                : analyticsRepository.countApplicationsReachedEachStage(userId)) {
            reachedCounts.put(ApplicationStatus.valueOf(row.getStatus()), row.getApplicationCount());
        }

        List<AnalyticsResponse.StageConversion> conversions = java.util.stream.IntStream
                .range(0, PIPELINE_STAGES.size() - 1)
                .mapToObj(index -> {
                    ApplicationStatus from = PIPELINE_STAGES.get(index);
                    ApplicationStatus to = PIPELINE_STAGES.get(index + 1);
                    long reachedFrom = reachedCounts.getOrDefault(from, 0L);
                    long converted = analyticsRepository.countApplicationsConvertedBetweenStages(
                            userId, from, to);
                    return new AnalyticsResponse.StageConversion(
                            from.name(),
                            to.name(),
                            reachedFrom,
                            converted,
                            percentage(converted, reachedFrom)
                    );
                })
                .toList();

        List<AnalyticsResponse.AverageDaysPerStage> averageDays = analyticsRepository
                .findAverageDaysPerStage(userId)
                .stream()
                .map(row -> new AnalyticsResponse.AverageDaysPerStage(
                        row.getStatus(), row.getAverageDays()))
                .toList();

        return new AnalyticsResponse(monthly, conversions, averageDays);
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
