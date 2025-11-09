package com.school.lending.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.lending.dto.EquipmentDto;
import com.school.lending.exception.ConflictException;
import com.school.lending.exception.DuplicateResourceException;
import com.school.lending.exception.InvalidInputException;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.Equipment;
import com.school.lending.repository.EquipmentRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
public class EquipmentService {

	private final EquipmentRepository equipmentRepository;

	public EquipmentService(EquipmentRepository equipmentRepository) {
		this.equipmentRepository = equipmentRepository;
	}

	public List<Equipment> getAll() {
		// TODO Auto-generated method stub
		return equipmentRepository.findAll();
	}

	public Optional<Equipment> getEquipmentById(Long id) {
		return equipmentRepository.findById(id);
	}

	public Equipment createEquipment(@Valid EquipmentDto equipment) {
		int totalQuantity = equipment.totalQuantity();
		if (totalQuantity < 0) {
			// Correct Error Handling: Throw a 400-mapped exception (InvalidInputException)
			throw new InvalidInputException("Initial quantity cannot be negative.");
		}
		String name = equipment.name();
		// Issue 1 Fix: Pass the name (String) to the repository method
		if (equipmentRepository.findByName(name).isPresent()) {
			// Correct Error Handling: Throw a 409-mapped exception
			// (DuplicateResourceException)
			throw new DuplicateResourceException("Equipment with this name already exists.");
		}
		Equipment newEquipment = Equipment.builder().totalQuantity(totalQuantity).borrowedCount(0)
				.availableQuantity(totalQuantity).name(name).category(equipment.category())
				.condition(equipment.condition()).build();
		return equipmentRepository.save(newEquipment);
	}

	public Equipment updateEquipment(Long id, @Valid EquipmentDto equipment) {
		Equipment existingEquipment = equipmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Equipment not found."));
		int currentBorrowed = existingEquipment.getBorrowedCount();
		int newTotalQuantity = equipment.totalQuantity();
		// CRITICAL VALIDATION: Check if the new total stock is less than the borrowed
		// count.
		if (newTotalQuantity < currentBorrowed) {
			throw new InvalidInputException("New total quantity (" + newTotalQuantity
					+ ") cannot be less than the number of items currently borrowed (" + currentBorrowed + ").");
		}
		existingEquipment.setName(equipment.name());
		existingEquipment.setCategory(equipment.category());
		existingEquipment.setCondition(equipment.condition());
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

    /**
     * Handles inventory adjustment when a borrow request is APPROVED.
     * Decrements available stock and increments borrowed count.
     * * @param equipment The Equipment entity being borrowed.
     * @param quantity The quantity being requested/approved.
     */
    @Transactional
    public void approveRequest(Equipment equipment, int quantity) {
        
        int currentAvailable = equipment.getAvailableQuantity();

        // 1. Critical Validation: Ensure stock hasn't dropped since the request was created.
        if (quantity > currentAvailable) {
            throw new InvalidInputException(
                "Cannot approve request. Requested quantity (" + quantity + 
                ") exceeds current available stock (" + currentAvailable + 
                ") for equipment: " + equipment.getName());
        }

        // 2. Update Inventory
        equipment.setAvailableQuantity(currentAvailable - quantity);
        equipment.setBorrowedCount(equipment.getBorrowedCount() + quantity);

        // 3. Save changes
        equipmentRepository.save(equipment);
    }

    /**
     * Handles inventory adjustment when a borrowed item is RETURNED.
     * Increments available stock and decrements borrowed count.
     * * @param equipment The Equipment entity being returned.
     * @param quantity The quantity being returned.
     */
    @Transactional
    public void returnRequest(Equipment equipment, int quantity) {
        
        int currentBorrowed = equipment.getBorrowedCount();
        
        // 1. Validation: Ensure we don't return more than what is currently marked as borrowed.
        if (quantity > currentBorrowed) {
            throw new InvalidInputException(
                "Cannot complete return. Quantity being returned (" + quantity + 
                ") exceeds currently marked borrowed quantity (" + currentBorrowed + 
                ") for equipment: " + equipment.getName());
        }

        // 2. Update Inventory
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + quantity);
        equipment.setBorrowedCount(currentBorrowed - quantity);

        // 3. Save changes
        equipmentRepository.save(equipment);
    }

}
