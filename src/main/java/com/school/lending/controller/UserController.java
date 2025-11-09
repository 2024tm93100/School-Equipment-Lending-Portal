package com.school.lending.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.lending.dto.LoginRequest;
import com.school.lending.dto.RegisterRequest;
import com.school.lending.dto.TokenRefreshRequest;
import com.school.lending.dto.TokenRefreshResponse;
import com.school.lending.dto.UserResponseDto;
import com.school.lending.exception.ResourceNotFoundException;
import com.school.lending.model.User;
import com.school.lending.service.KeycloakUserService;
import com.school.lending.service.UserService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;


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

	@PostMapping("/auth/login")
	public ResponseEntity<Map<String, Object>> autheticateUser(@RequestBody LoginRequest request) {
		String email = request.email();
		Map<String, Object> token = keycloakAuthService.getToken(email, request.password());
		return ResponseEntity.ok(token);
	}

	@PostMapping("/auth/refresh")
	public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
		String requestRefreshToken = request.refreshToken();

		Map<String, Object> tokenMap = keycloakAuthService.refreshToken(requestRefreshToken);

		String newAccessToken = (String) tokenMap.get("access_token");
		String newRefreshToken = (String) tokenMap.get("refresh_token");

		// Keycloak returns these as Long/Integer. We cast them to Long.
		Long newExpiresIn = ((Number) tokenMap.get("expires_in")).longValue();
		Long newRefreshExpiresIn = ((Number) tokenMap.get("refresh_expires_in")).longValue();

		return ResponseEntity
				.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken, newExpiresIn, newRefreshExpiresIn));
	}

	@PostMapping("/auth/register")
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

	// 💡 Frontend expects the GET /users/me endpoint
    @GetMapping("/users/me")
    public Map<String, Object> getUserDetails(Principal principal) {
        // 1. Get the authenticated JWT object from the Security Context
        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principalObject instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("email");
            // 2. Extract application claims from the JWT payload
            Map<String, Object> userDetails = new HashMap<>();
			User user = userService.getUserByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Equipment not found with ID: " + email));
			userDetails.put("id", user.getUserId());
			userDetails.put("firstName", user.getFirstName());
			userDetails.put("lastName", user.getLastName());
            // The 'sub' (subject) claim is the unique user ID from Keycloak
            userDetails.put("keyCloakId", jwt.getClaimAsString("sub")); 
            
            // The 'preferred_username' is the username/staff ID
            userDetails.put("uuid", principal.getName());
            // The 'email' claim is usually available
            userDetails.put("email", email);
			userDetails.put("preferred_username", jwt.getClaimAsString("preferred_username"));
            userDetails.put("name", jwt.getClaimAsString("name")); 
            userDetails.put("firstName", jwt.getClaimAsString("given_name"));
            userDetails.put("lastName", jwt.getClaimAsString("family_name"));
            // Add the role that was previously determined and stored (e.g., from the token logic)
            // Assuming you have a way to retrieve the single application role (e.g., "STAFF")
            // This is crucial for your frontend logic!
            Map<String,Object> realmAccess = jwt.getClaimAsMap("realm_access");
            List<String> roles = realmAccess != null ? (List<String>) realmAccess.get("roles") : List.of();
            
            // 3. Find and add the application role (ADMIN, STAFF, or STUDENT)
            String appRole = roles.stream()
                .filter(r -> r.equals("ADMIN") || r.equals("STAFF") || r.equals("STUDENT"))
                .findFirst()
                .orElse("STUDENT"); // Default to STUDENT if none found

            userDetails.put("role", appRole);
            return userDetails;
        }

        // Fallback for non-JWT authentication
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("preferred_username", principal.getName());
        return fallback;
    }
	
}
