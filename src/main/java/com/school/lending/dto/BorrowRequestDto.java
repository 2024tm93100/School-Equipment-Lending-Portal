package com.school.lending.dto;

import java.time.LocalDate;

import com.school.lending.model.RequestStatus;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BorrowRequestDto(@NotNull Long userId, @NotNull Long equipmentId, @Min(1) int requestedQuantity,
		@NotNull @FutureOrPresent LocalDate startDate, @NotNull @Future LocalDate endDate, RequestStatus status) {

	public BorrowRequestDto {
		if (startDate != null && endDate != null && !endDate.isAfter(startDate)) {
			throw new IllegalArgumentException("End date must be after start date.");
		}
	}
}
