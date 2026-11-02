package com.jobtracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalApplications,
        Map<String, Long> applicationsByStatus,
        LocalDate weekStart,
        LocalDate weekEnd,
        List<InterviewResponse> upcomingInterviews,
        BigDecimal offerRatePercent,
        BigDecimal responseRatePercent
) {
}
