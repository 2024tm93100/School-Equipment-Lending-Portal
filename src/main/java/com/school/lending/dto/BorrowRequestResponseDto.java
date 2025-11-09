package com.school.lending.dto;

import java.time.LocalDate;

import com.school.lending.model.RequestStatus;

/**
 * Data Transfer Object (DTO) for displaying a student's borrow request list.
 * This record is used to safely serialize data to the frontend.
 */
public record BorrowRequestResponseDto(
        // Core Request Fields
        Long requestId,
        Long equipmentId,
        Long userId,
        String equipmentName,
        int requestedQuantity,
        LocalDate startDate,
        LocalDate endDate,
        RequestStatus status) {
    public boolean isValid() {
        return equipmentId != null;
    }
}