package com.school.lending.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.lending.dto.BorrowRequestDto;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.BorrowRequest;
import com.school.lending.service.BorrowRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BorrowRequestController {

	private BorrowRequestService borrowRequestService;

	@GetMapping("/requests")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<List<BorrowRequest>> getAllRequests() {
		List<BorrowRequest> requestsList = borrowRequestService.getAll();
		if (requestsList.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(requestsList);
	}

	@GetMapping("/requests/{id}")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<BorrowRequest> getEquipmentById(@PathVariable Long id) {
		BorrowRequest request = borrowRequestService.getRequestById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
		return ResponseEntity.ok(request);
	}

	@GetMapping("/requests/user/{userId}")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<List<BorrowRequest>> getRequestByUserId(@PathVariable Long userId) {
		List<BorrowRequest> requestList = borrowRequestService.getAllRequestOfUser(userId);
		return ResponseEntity.ok(requestList);
	}

	@PostMapping("/requests")
	@PreAuthorize("hasAnyRole('STUDENT')")
	public ResponseEntity<?> createNewRequest(@RequestBody @Valid BorrowRequestDto newRequest) {
		BorrowRequest newRequestCreated = borrowRequestService.createRequest(newRequest);
		return ResponseEntity.created(null).body(mapToDto(newRequestCreated));
	}

	private BorrowRequestDto mapToDto(BorrowRequest req) {
		return new BorrowRequestDto(req.getUser().getUserId(), req.getEquipment().getEquipmentId(),
				req.getRequestedQuantity(), req.getStartDate(), req.getEndDate(), req.getStatus());
	}

	@PutMapping("/requests/{id}")
	@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
	public ResponseEntity<?> updateRequestStatus(@PathVariable Long id, @RequestBody @Valid BorrowRequestDto request) {
		BorrowRequest updatedRequest = borrowRequestService.updateRequest(id, request);
		return ResponseEntity.ok(mapToDto(updatedRequest));
	}

	@DeleteMapping("/requests/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
		borrowRequestService.deleteRequest(id);
		return ResponseEntity.noContent().build();
	}
}
