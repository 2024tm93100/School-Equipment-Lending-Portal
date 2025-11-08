package com.school.lending.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.school.lending.dto.BorrowRequestDto;
import com.school.lending.exception.InvalidInputException;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.BorrowRequest;
import com.school.lending.model.Equipment;
import com.school.lending.model.RequestStatus;
import com.school.lending.model.User;
import com.school.lending.repository.BorrowRequestRepository;
import com.school.lending.repository.EquipmentRepository;
import com.school.lending.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;

@Service
public class BorrowRequestService {

	private final BorrowRequestRepository borrowRequestRepository;
	private UserService userService;
	private EquipmentService equipmentService;

	public BorrowRequestService(BorrowRequestRepository borrowRequestRepository,
			EquipmentRepository equipmentRepository, UserRepository userRepository) {
		this.borrowRequestRepository = borrowRequestRepository;
	}

	public List<BorrowRequest> getAll() {
		return borrowRequestRepository.findAll();
	}

	public Optional<BorrowRequest> getRequestById(Long id) {
		return borrowRequestRepository.findById(id);
	}

	@Transactional
	public BorrowRequest createRequest(@Valid BorrowRequestDto newRequest) {
		// Validation of Request Fields (DTO Check)
		if (newRequest.requestedQuantity() <= 0) {
			throw new InvalidInputException("Request quantity cannot be zero or negative.");
		}
		// Validation of References (Database Checks)
		User user = userService.getUserById(newRequest.userId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + newRequest.userId()));
		Equipment equipment = equipmentService.getEquipmentById(newRequest.equipmentId()).orElseThrow(
				() -> new ResourceNotFoundException("Equipment not found with ID: " + newRequest.equipmentId()));

		// Validation of Dates (Business Logic Check)
		LocalDate now = LocalDate.now();
		LocalDate startDate = newRequest.startDate();
		LocalDate endDate = newRequest.endDate();

		if (startDate.isBefore(now)) {
			throw new BadRequestException("Start date cannot be in the past");
		}
		if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
			throw new BadRequestException("End date must be after the start date.");
		}

		int requestedQuantity = newRequest.requestedQuantity();
		int availableStock = equipment.getAvailableQuantity();

		if (requestedQuantity > availableStock) {
			throw new BadRequestException("Requested quantity (" + requestedQuantity + ") exceeds available stock ("
					+ availableStock + ") for equipment: " + equipment.getName());
		}

		BorrowRequest newlyCreatedRequest = BorrowRequest.builder().user(user).equipment(equipment)
				.requestedQuantity(requestedQuantity).startDate(startDate).endDate(endDate).requestDate(now)
				.status(RequestStatus.PENDING).build();
		return borrowRequestRepository.save(newlyCreatedRequest);
	}

	public BorrowRequest updateRequest(Long id, @Valid BorrowRequestDto request) {

		// Validation of References (Database Checks)
		User user = userService.getUserById(request.userId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.userId()));
		Equipment equipment = equipmentService.getEquipmentById(request.equipmentId()).orElseThrow(
				() -> new ResourceNotFoundException("Equipment not found with ID: " + request.equipmentId()));

		BorrowRequest existingRequest = borrowRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));

		BorrowRequest updatedRequest = BorrowRequest.builder().user(user).equipment(equipment)
				.requestedQuantity(request.requestedQuantity()).startDate(request.startDate())
				.endDate(request.endDate()).requestDate(existingRequest.getRequestDate()).status(request.status())
				.build();
		return borrowRequestRepository.save(updatedRequest);
	}

	public void deleteRequest(Long id) {
		// Check if the request exists (and get it)
		BorrowRequest existingEquipment = borrowRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));
		borrowRequestRepository.delete(existingEquipment);
	}

	public List<BorrowRequest> getAllRequestOfUser(Long userId) {
		User user = userService.getUserById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		return borrowRequestRepository.findAllByUser(user);
	}

}
