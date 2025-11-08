package com.school.lending.service;

import org.springframework.stereotype.Service;

import com.school.lending.dto.RegisterRequest;
import com.school.lending.model.Role;
import com.school.lending.model.User;
import com.school.lending.repository.UserRepository;

@Service
public class UserService {
	private UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User registerUserLocally(RegisterRequest request) {
		User newUser = new User();
		newUser.setName(request.username());
		newUser.setEmail(request.email());
		try {
			Role roleEnum = Role.valueOf(request.role().toUpperCase());
			newUser.setRole(roleEnum);
		} catch (IllegalArgumentException e) {
			// Handle case where the request.role() string doesn't match any enum constant
			throw new IllegalArgumentException("Invalid role specified: " + request.role());
		}
		return userRepository.save(newUser);
	}

}
