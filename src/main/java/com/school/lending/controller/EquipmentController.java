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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.school.lending.dto.EquipmentDto;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.Equipment;
import com.school.lending.service.EquipmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class EquipmentController {
	private final EquipmentService equipmentService;

	public EquipmentController(EquipmentService equipmentService) {
		this.equipmentService = equipmentService;
	}

	@GetMapping("/equipment")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<List<Equipment>> getAllEquipment() {
		List<Equipment> equipmentList = equipmentService.getAll();
		if (equipmentList.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(equipmentList);
	}

	@GetMapping("/equipment/{id}")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
		Equipment equipment = equipmentService.getEquipmentById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
		return ResponseEntity.ok(equipment);
	}

	@PostMapping("/equipment")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Equipment> createNewEquipment(@RequestBody @Valid EquipmentDto newEquipment) {
		Equipment createdEquipment = equipmentService.createEquipment(newEquipment);
		return ResponseEntity.created(null).body(createdEquipment);
	}

	@PutMapping("/equipment/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id,
			@RequestBody @Valid EquipmentDto equipment) {
		Equipment updatedEquipment = equipmentService.updateEquipment(id, equipment);
		return ResponseEntity.ok(updatedEquipment);
	}

	@GetMapping("/equipment/search")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<List<Equipment>> searchEquipmentByCategory(
			@RequestParam(value = "category") String categoryName) {

		List<Equipment> results = equipmentService.searchByCategory(categoryName);

		// Returns 200 OK, even if the list is empty
		return ResponseEntity.ok(results);
	}

	@DeleteMapping("/equipment/{id}")
	@PreAuthorize("hasAnyRole('ADMIN')")
	public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {

		// The service layer handles:
		// 1. Finding the resource (throws 404 if not found).
		// 2. Checking the borrowed count (throws 409 Conflict if borrowed).
		equipmentService.deleteEquipment(id);

		// Returns HTTP 204 No Content for a successful deletion with no body.
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/equipment/available")
	@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
	public ResponseEntity<List<Equipment>> getAvailableEquipment() {

		// The service layer handles filtering and calculating available quantity.
		List<Equipment> availableList = equipmentService.getAvailableEquipment();

		// Returns HTTP 200 OK with the list (which may be empty).
		return ResponseEntity.ok(availableList);
	}

}
