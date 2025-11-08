package com.school.lending.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long equipmentId;
	private String name;
	private String category;
	private EquipmentCondition condition;
	private int totalQuantity;
	private int borrowedCount;
	private int availableQuantity;

	public Long getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(Long equipmentId) {
		this.equipmentId = equipmentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public EquipmentCondition getCondition() {
		return condition;
	}

	public void setCondition(EquipmentCondition condition) {
		this.condition = condition;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public int getBorrowedCount() {
		return borrowedCount;
	}

	public void setBorrowedCount(int borrowedCount) {
		this.borrowedCount = borrowedCount;
	}

	public int getAvailableQuantity() {
		return availableQuantity;
	}

	public void setAvailableQuantity(int availableQuantity) {
		this.availableQuantity = availableQuantity;
	}

}