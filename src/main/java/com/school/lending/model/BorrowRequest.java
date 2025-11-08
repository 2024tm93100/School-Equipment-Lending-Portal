package com.school.lending.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "borrow_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long requestId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "equipment_id", nullable = false)
	private Equipment equipment;

	@Column(nullable = false)
	private int requestedQuantity;

	@Column(nullable = false)
	private LocalDate requestDate = LocalDate.now();

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private RequestStatus status; // PENDING, APPROVED, REJECTED, RETURNED
}
