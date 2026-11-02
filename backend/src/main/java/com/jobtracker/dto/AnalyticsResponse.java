package com.jobtracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AnalyticsResponse(
        List<MonthlyApplications> applicationsPerMonth,
        List<StageConversion> conversionRates,
        List<AverageDaysPerStage> averageDaysPerStage
) {
    public record MonthlyApplications(LocalDate month, long applicationCount) {
    }

    public record StageConversion(
            String fromStage,
            String toStage,
            long applicationsReachedFromStage,
            long applicationsConverted,
            BigDecimal conversionRatePercent
    ) {
    }

    public record AverageDaysPerStage(String status, BigDecimal averageDays) {
    }
}
