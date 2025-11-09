package com.school.lending.dto;

/**
 * DTO representing key summary statistics for the Admin/Staff dashboard.
 */
public record AnalyticsSummaryDto(
    long totalEquipment,
    long pendingRequestsCount,
    long availableItems
) {
}