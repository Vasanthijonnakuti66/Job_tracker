package com.jobtracker.service;

import com.jobtracker.dto.DashboardResponse;
import com.jobtracker.dto.InterviewResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.InterviewRepository;
import com.jobtracker.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public DashboardService(ApplicationRepository applicationRepository,
                            InterviewRepository interviewRepository,
                            StatusHistoryRepository statusHistoryRepository) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    public DashboardResponse getDashboard(Long userId) {
        long total = applicationRepository.countByUserId(userId);
        Map<String, Long> countsByStatus = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            countsByStatus.put(status.name(),
                    applicationRepository.countByUserIdAndStatus(userId, status));
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        List<InterviewResponse> upcomingInterviews = interviewRepository
                .findUpcomingByUserId(userId, today, weekEnd)
                .stream()
                .map(InterviewResponse::from)
                .toList();

        long offers = statusHistoryRepository.countApplicationsThatReachedOffer(userId);
        long responded = statusHistoryRepository.countApplicationsWithAResponse(userId);

        return new DashboardResponse(
                total,
                countsByStatus,
                weekStart,
                weekEnd,
                upcomingInterviews,
                percentage(offers, total),
                percentage(responded, total)
        );
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
