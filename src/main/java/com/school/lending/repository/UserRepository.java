package com.school.lending.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.lending.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
