package com.jobtracker.service;

import com.jobtracker.dto.DashboardResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.InterviewRepository;
import com.jobtracker.repository.StatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    private DashboardService service;

    @BeforeEach
    void setUp() {
        service = new DashboardService(
                applicationRepository, interviewRepository, statusHistoryRepository);
    }

    @Test
    void dashboardCalculatesRatesFromHistoricalResponses() {
        when(applicationRepository.countByUserId(8L)).thenReturn(4L);
        when(applicationRepository.countByUserIdAndStatus(8L, ApplicationStatus.APPLIED))
                .thenReturn(1L);
        when(applicationRepository.countByUserIdAndStatus(8L, ApplicationStatus.ASSESSMENT))
                .thenReturn(1L);
        when(applicationRepository.countByUserIdAndStatus(8L, ApplicationStatus.INTERVIEW))
                .thenReturn(1L);
        when(applicationRepository.countByUserIdAndStatus(8L, ApplicationStatus.OFFER))
                .thenReturn(1L);
        when(applicationRepository.countByUserIdAndStatus(8L, ApplicationStatus.REJECTED))
                .thenReturn(0L);
        when(statusHistoryRepository.countApplicationsThatReachedOffer(8L)).thenReturn(1L);
        when(statusHistoryRepository.countApplicationsWithAResponse(8L)).thenReturn(3L);
        when(interviewRepository.findUpcomingByUserId(
                org.mockito.ArgumentMatchers.eq(8L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        DashboardResponse response = service.getDashboard(8L);

        assertEquals(4L, response.totalApplications());
        assertEquals(1L, response.applicationsByStatus().get("OFFER"));
        assertEquals(new BigDecimal("25.00"), response.offerRatePercent());
        assertEquals(new BigDecimal("75.00"), response.responseRatePercent());
        assertEquals(List.of(), response.upcomingInterviews());
        assertEquals(7, response.weekEnd().getDayOfWeek().getValue());
        assertEquals(1, response.weekStart().getDayOfWeek().getValue());
    }

    @Test
    void dashboardReturnsZeroRatesWhenUserHasNoApplications() {
        when(applicationRepository.countByUserId(9L)).thenReturn(0L);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            when(applicationRepository.countByUserIdAndStatus(9L, status)).thenReturn(0L);
        }
        when(statusHistoryRepository.countApplicationsThatReachedOffer(9L)).thenReturn(0L);
        when(statusHistoryRepository.countApplicationsWithAResponse(9L)).thenReturn(0L);
        when(interviewRepository.findUpcomingByUserId(
                org.mockito.ArgumentMatchers.eq(9L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        DashboardResponse response = service.getDashboard(9L);

        assertEquals(new BigDecimal("0.00"), response.offerRatePercent());
        assertEquals(new BigDecimal("0.00"), response.responseRatePercent());
    }
}
