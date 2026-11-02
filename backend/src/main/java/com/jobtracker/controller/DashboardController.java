package com.jobtracker.controller;

import com.jobtracker.dto.AnalyticsResponse;
import com.jobtracker.dto.DashboardResponse;
import com.jobtracker.security.UserPrincipal;
import com.jobtracker.service.AnalyticsService;
import com.jobtracker.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AnalyticsService analyticsService;

    public DashboardController(DashboardService dashboardService,
                               AnalyticsService analyticsService) {
        this.dashboardService = dashboardService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getDashboard(currentUser.getId()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(analyticsService.getAnalytics(currentUser.getId()));
    }
}
