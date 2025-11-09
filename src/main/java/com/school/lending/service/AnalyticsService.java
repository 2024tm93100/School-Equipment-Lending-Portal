package com.school.lending.service;

import com.school.lending.dto.AnalyticsSummaryDto;
import com.school.lending.model.RequestStatus;
import com.school.lending.repository.BorrowRequestRepository;
import com.school.lending.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final EquipmentRepository equipmentRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    // Constructor Injection
    public AnalyticsService(EquipmentRepository equipmentRepository, BorrowRequestRepository borrowRequestRepository) {
        this.equipmentRepository = equipmentRepository;
        this.borrowRequestRepository = borrowRequestRepository;
    }

    /**
     * Aggregates key statistics for the dashboard summary.
     */
    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getSummaryAnalytics() {
        
        // 1. Total Equipment (Total number of unique items/rows in the Equipment table)
        // You'll need to define count() in your repository if it doesn't exist (it should via JpaRepository)
        long totalEquipment = equipmentRepository.count();

        // 2. Pending Requests Count
        // You need to define this method in BorrowRequestRepository: countByStatus(RequestStatus status)
        long pendingRequestsCount = borrowRequestRepository.countByStatus(RequestStatus.PENDING);

        // 3. Available Items (Sum of availableQuantity across all equipment)
        // You'll need a custom method in EquipmentRepository: sumAvailableQuantity()
        // We use .orElse(0L) for safety if the sum returns null
        Long availableItems = equipmentRepository.sumAvailableQuantity().orElse(0L); 
        
        return new AnalyticsSummaryDto(
            totalEquipment,
            pendingRequestsCount,
            availableItems
        );
    }
}