package com.jobtracker.service;

import com.jobtracker.dto.AnalyticsResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.repository.AnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(analyticsRepository);
    }

    @Test
    void analyticsMapsAggregatesAndComputesStageConversionPercentages() {
        AnalyticsRepository.MonthlyApplicationsRow monthly =
                mock(AnalyticsRepository.MonthlyApplicationsRow.class);
        when(monthly.getMonth()).thenReturn(LocalDate.of(2026, 10, 1));
        when(monthly.getApplicationCount()).thenReturn(6L);
        when(analyticsRepository.findApplicationsPerMonth(7L)).thenReturn(List.of(monthly));

        AnalyticsRepository.ApplicationsReachedStageRow applied = reached("APPLIED", 10L);
        AnalyticsRepository.ApplicationsReachedStageRow assessment = reached("ASSESSMENT", 5L);
        AnalyticsRepository.ApplicationsReachedStageRow interview = reached("INTERVIEW", 2L);
        when(analyticsRepository.countApplicationsReachedEachStage(7L))
                .thenReturn(List.of(applied, assessment, interview));
        when(analyticsRepository.countApplicationsConvertedBetweenStages(
                7L, ApplicationStatus.APPLIED, ApplicationStatus.ASSESSMENT)).thenReturn(4L);
        when(analyticsRepository.countApplicationsConvertedBetweenStages(
                7L, ApplicationStatus.ASSESSMENT, ApplicationStatus.INTERVIEW)).thenReturn(2L);
        when(analyticsRepository.countApplicationsConvertedBetweenStages(
                7L, ApplicationStatus.INTERVIEW, ApplicationStatus.OFFER)).thenReturn(0L);

        AnalyticsRepository.AverageDaysPerStageRow duration =
                mock(AnalyticsRepository.AverageDaysPerStageRow.class);
        when(duration.getStatus()).thenReturn("INTERVIEW");
        when(duration.getAverageDays()).thenReturn(new BigDecimal("3.50"));
        when(analyticsRepository.findAverageDaysPerStage(7L)).thenReturn(List.of(duration));

        AnalyticsResponse response = service.getAnalytics(7L);

        assertEquals(new AnalyticsResponse.MonthlyApplications(
                LocalDate.of(2026, 10, 1), 6L), response.applicationsPerMonth().get(0));
        assertEquals(new BigDecimal("40.00"),
                response.conversionRates().get(0).conversionRatePercent());
        assertEquals(new BigDecimal("40.00"),
                response.conversionRates().get(1).conversionRatePercent());
        assertEquals(new BigDecimal("0.00"),
                response.conversionRates().get(2).conversionRatePercent());
        assertEquals("INTERVIEW", response.averageDaysPerStage().get(0).status());
        verify(analyticsRepository).countApplicationsConvertedBetweenStages(
                eq(7L), eq(ApplicationStatus.INTERVIEW), eq(ApplicationStatus.OFFER));
    }

    private AnalyticsRepository.ApplicationsReachedStageRow reached(String status, long count) {
        AnalyticsRepository.ApplicationsReachedStageRow row =
                mock(AnalyticsRepository.ApplicationsReachedStageRow.class);
        when(row.getStatus()).thenReturn(status);
        when(row.getApplicationCount()).thenReturn(count);
        return row;
    }
}
