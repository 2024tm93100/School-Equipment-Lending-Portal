package com.school.lending.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.school.lending.model.Equipment;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

	Optional<Equipment> findByName(String name);

	// Find all equipment that matches the given category name
	List<Equipment> findByCategory(String category);

    // JpaRepository provides count() for total equipment count

    @Query("SELECT SUM(e.availableQuantity) FROM Equipment e")
    Optional<Long> sumAvailableQuantity();

	// Optional: For keyword search across Name or Condition (Requires @Query if
	// complex)
	// List<Equipment>
	// findByNameContainingIgnoreCaseOrConditionContainingIgnoreCase(String name,
	// String condition);
}
