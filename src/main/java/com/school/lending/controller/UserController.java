package com.school.lending.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.lending.dto.LoginRequest;
import com.school.lending.dto.RegisterRequest;
import com.school.lending.service.KeycloakUserService;
import com.school.lending.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

	private final KeycloakUserService keycloakAuthService;
	private final UserService userService;

	public UserController(KeycloakUserService keycloakAuthService, UserService userService) {
		this.keycloakAuthService = keycloakAuthService;
		this.userService = userService;
	}

	// Accessible by ADMIN only
	@GetMapping("/admin/hello")
	public Map<String, Object> admin(@AuthenticationPrincipal Jwt jwt) {
		return Map.of("endpoint", "/api/admin/hello", "role", "ADMIN", "user",
				jwt.getClaimAsString("preferred_username"), "claims", jwt.getClaims());
	}

	// Accessible by STAFF and ADMIN
	@GetMapping("/staff/hello")
	public Map<String, Object> staff(@AuthenticationPrincipal Jwt jwt) {
		return Map.of("endpoint", "/api/staff/hello", "role", "STAFF or ADMIN", "user",
				jwt.getClaimAsString("preferred_username"), "claims", jwt.getClaims());
	}

	@PostMapping("/token")
	public ResponseEntity<Map<String, Object>> getToken(@RequestBody LoginRequest request) {
		Map<String, Object> token = keycloakAuthService.getToken(request.username(), request.password());
		return ResponseEntity.ok(token);
	}

	@PostMapping("/register")
	public ResponseEntity<Object> registerUser(@RequestBody RegisterRequest request) {
		try {
			// 1. Create the user in Keycloak
			keycloakAuthService.createUserAndAssignDefaultRole(request);

			// 2. Register the user details in your local database
			userService.registerUserLocally(request);

			return ResponseEntity.ok("User Registered successfully in Keycloak and local DB!");

		} catch (RuntimeException e) {
			// Log the error (e.g., Keycloak failed to create user, or DB failed)
			System.err.println("Registration error: " + e.getMessage());
			return ResponseEntity.status(500).body("User registration failed: " + e.getMessage());
		}
	}
}
