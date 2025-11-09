package com.school.lending.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.lending.model.BorrowRequest;
import com.school.lending.model.RequestStatus;
import com.school.lending.model.User;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

	List<BorrowRequest> findAllByUser(User user);

    List<BorrowRequest> findAllByStatus(RequestStatus filterStatus);

    long countByStatus(RequestStatus pending);

}
