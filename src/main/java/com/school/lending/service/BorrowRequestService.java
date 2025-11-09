package com.school.lending.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.lending.dto.BorrowRequestDto;
import com.school.lending.dto.BorrowRequestResponseDto;
import com.school.lending.exception.InvalidInputException;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.BorrowRequest;
import com.school.lending.model.Equipment;
import com.school.lending.model.RequestStatus;
import com.school.lending.model.User;
import com.school.lending.repository.BorrowRequestRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;

@Service
public class BorrowRequestService {

	private final BorrowRequestRepository borrowRequestRepository;
	private final UserService userService;
	private final EquipmentService equipmentService;

	public BorrowRequestService(BorrowRequestRepository borrowRequestRepository, UserService userService,
			EquipmentService equipmentService) {
		this.borrowRequestRepository = borrowRequestRepository;
		this.userService = userService;
		this.equipmentService = equipmentService;
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

	@Transactional
	public BorrowRequest updateRequest(Long id, @Valid BorrowRequestDto requestDto) {
		// Validation of References (Database Checks)
		User user = userService.getUserById(requestDto.userId())
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + requestDto.userId()));
		Equipment equipment = equipmentService.getEquipmentById(requestDto.equipmentId()).orElseThrow(
				() -> new ResourceNotFoundException("Equipment not found with ID: " + requestDto.equipmentId()));

		BorrowRequest existingRequest = borrowRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));

		// BorrowRequest updatedRequest = BorrowRequest.builder().user(user).equipment(equipment)
		// 		.requestedQuantity(request.requestedQuantity()).startDate(request.startDate())
		// 		.endDate(request.endDate()).requestDate(existingRequest.getRequestDate()).status(request.status())
		// 		.build();
		// System.out.println("========dbcsjcbksa======="+ request.status());
		// return borrowRequestRepository.save(updatedRequest);
		// 💡 FIX 2: Implement core business logic for status change
        RequestStatus oldStatus = existingRequest.getStatus();
        RequestStatus newStatus = requestDto.status();

        if (oldStatus == RequestStatus.PENDING && newStatus == RequestStatus.APPROVED) {
            // Approval Logic: Validate stock and update inventory
            equipmentService.approveRequest(equipment, requestDto.requestedQuantity()); 
        
        } else if (oldStatus == RequestStatus.APPROVED && newStatus == RequestStatus.RETURNED) {
            // Return Logic: Restore inventory
            equipmentService.returnRequest(equipment, requestDto.requestedQuantity());

        } else if (oldStatus == RequestStatus.PENDING && newStatus == RequestStatus.REJECTED) {
            // Rejection Logic: No inventory update needed
            // (Only status change will happen below)
        } else {
            // Optional: Throw error for invalid state transitions (e.g., APPROVED -> PENDING)
            throw new InvalidInputException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        // Update essential fields and status on the existing entity
        // We only update status here; the other fields (dates, quantity) usually shouldn't change
        existingRequest.setStatus(newStatus);
        
        // This is a more robust way to update an existing entity than building a new one
        return borrowRequestRepository.save(existingRequest);
	}

	public void deleteRequest(Long id) {
		// Check if the request exists (and get it)
		BorrowRequest existingEquipment = borrowRequestRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found with ID: " + id));
		borrowRequestRepository.delete(existingEquipment);
	}

	private BorrowRequestResponseDto convertToDto(BorrowRequest entity) {
		return new BorrowRequestResponseDto(
				entity.getRequestId(),
				entity.getEquipment().getEquipmentId(),
				entity.getUser().getUserId(),
				entity.getEquipment().getName(),
				entity.getRequestedQuantity(),
				entity.getStartDate(),
				entity.getEndDate(),
				entity.getStatus());
	}

	@Transactional
	public List<BorrowRequestResponseDto> getAllRequestOfUser(Long userId) {
		User user = userService.getUserById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		List<BorrowRequest> requests = borrowRequestRepository.findAllByUser(user);

		// 💡 Convert to DTOs immediately before returning
		return requests.stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
	}

	public List<BorrowRequestResponseDto> getRequestsByStatus(RequestStatus filterStatus) {
		List<BorrowRequest> requests = borrowRequestRepository.findAllByStatus(filterStatus);

		// 💡 Convert to DTOs immediately before returning
		return requests.stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
	}

}
