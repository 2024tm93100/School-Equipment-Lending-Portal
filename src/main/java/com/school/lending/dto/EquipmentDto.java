package com.school.lending.dto;

import com.school.lending.model.EquipmentCondition;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EquipmentDto(@NotNull String name, @NotNull String category, @NotNull EquipmentCondition condition,
		@Min(1) int totalQuantity, @Min(0) int borrowedCount, @Min(0) int availableQuantity) {

}
