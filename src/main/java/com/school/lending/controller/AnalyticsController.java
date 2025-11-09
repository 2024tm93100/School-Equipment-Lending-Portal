package com.school.lending.controller;

import com.school.lending.dto.AnalyticsSummaryDto;
import com.school.lending.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Constructor Injection
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * GET /api/analytics/summary
     * Returns key metrics for the Admin/Staff dashboard.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')") // Restrict access to authorized staff/admin
    public ResponseEntity<AnalyticsSummaryDto> getSummary() {
        AnalyticsSummaryDto summary = analyticsService.getSummaryAnalytics();
        return ResponseEntity.ok(summary);
    }
}