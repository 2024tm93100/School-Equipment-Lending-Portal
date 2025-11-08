package com.school.lending.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.lending.exception.ConflictException;
import com.school.lending.exception.DuplicateResourceException;
import com.school.lending.exception.InvalidInputException;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.Equipment;
import com.school.lending.repository.EquipmentRepository;

import jakarta.validation.Valid;

@Service
public class EquipmentService {

	private EquipmentRepository equipmentRepository;

	public EquipmentService() {

	}

	public List<Equipment> getAll() {
		// TODO Auto-generated method stub
		return equipmentRepository.findAll();
	}

	public Optional<Equipment> getEquipmentById(Long id) {
		return equipmentRepository.findById(id);
	}

	public Equipment createEquipment(Equipment equipment) {
		if (equipment.getTotalQuantity() < 0) {
			// Correct Error Handling: Throw a 400-mapped exception (InvalidInputException)
			throw new InvalidInputException("Initial quantity cannot be negative.");
		}

		// Issue 1 Fix: Pass the name (String) to the repository method
		if (equipmentRepository.findByName(equipment.getName()).isPresent()) {
			// Correct Error Handling: Throw a 409-mapped exception
			// (DuplicateResourceException)
			throw new DuplicateResourceException("Equipment with this name already exists.");
		}
		equipment.setBorrowedCount(0);
		equipment.setAvailableQuantity(equipment.getTotalQuantity());
		// If all checks pass, save the equipment
		return equipmentRepository.save(equipment);
	}

	public Equipment updateEquipment(Long id, @Valid Equipment equipment) {
		Equipment existingEquipment = equipmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Equipment not found."));
		int currentBorrowed = existingEquipment.getBorrowedCount();
		int newTotalQuantity = equipment.getTotalQuantity();
		// CRITICAL VALIDATION: Check if the new total stock is less than the borrowed
		// count.
		if (newTotalQuantity < currentBorrowed) {
			throw new InvalidInputException("New total quantity (" + newTotalQuantity
					+ ") cannot be less than the number of items currently borrowed (" + currentBorrowed + ").");
		}
		existingEquipment.setName(equipment.getName());
		existingEquipment.setCategory(equipment.getCategory());
		existingEquipment.setCondition(equipment.getCondition());
		existingEquipment.setTotalQuantity(newTotalQuantity);
		existingEquipment.setAvailableQuantity(newTotalQuantity - currentBorrowed);
		return equipmentRepository.save(existingEquipment);
	}

	public void deleteEquipment(Long id) {

		// 1. Check if the equipment exists (and get it)
		Equipment existingEquipment = equipmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Equipment not found with ID: " + id));

		// 2. CRITICAL CHECK: Prevent deletion if any units are borrowed
		// Assuming 'getBorrowedCount()' exists on your Equipment entity
		if (existingEquipment.getBorrowedCount() > 0) {
			throw new ConflictException("Cannot delete equipment. " + existingEquipment.getBorrowedCount()
					+ " unit(s) are currently borrowed.");
		}

		// 3. Perform the deletion
		equipmentRepository.delete(existingEquipment);
	}

	public List<Equipment> getAvailableEquipment() {

		// Find all equipment and stream the results
		return equipmentRepository.findAll().stream()
				// Filter out items where (totalQuantity - borrowedCount) is zero or less
				.filter(equipment -> equipment.getTotalQuantity() > equipment.getBorrowedCount())
				// Recalculate and set the availableQuantity field for the final JSON output
				// (optional, but good practice)
				.peek(equipment -> equipment
						.setAvailableQuantity(equipment.getTotalQuantity() - equipment.getBorrowedCount()))
				.collect(Collectors.toList());
	}

	public List<Equipment> searchByCategory(String categoryName) {
		// Calls the custom repository method
		return equipmentRepository.findByCategory(categoryName);
	}

}
