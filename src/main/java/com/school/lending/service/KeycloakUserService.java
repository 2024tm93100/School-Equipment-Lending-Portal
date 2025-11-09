package com.school.lending.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.lending.dto.RegisterRequest;
import com.school.lending.exception.AuthenticationException;

@Service
public class KeycloakUserService {

	private String keycloakServerUrl = "http://localhost:8081/";

	private String realm = "school";

	@Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
	private String clientId;

	@Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
	private String clientSecret;

	private final ObjectMapper objectMapper;

	public KeycloakUserService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public Map<String, Object> getToken(String username, String password) {
		String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", clientId);
		formData.add("client_secret", clientSecret);
		formData.add("grant_type", "password");
		formData.add("username", username);
		formData.add("password", password);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
		try {
			ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

			// If successful, return the token map
			return response.getBody();
		} catch (HttpClientErrorException e) {

			HttpStatus statusCode = (HttpStatus) e.getStatusCode();

			// Check for 400 (Standard OIDC invalid_grant) OR 401 (Your Keycloak setup's
			// response)
			if (statusCode == HttpStatus.BAD_REQUEST || statusCode == HttpStatus.UNAUTHORIZED) {
				String errorMessage = "Invalid username or password.";

				try {
					Map<String, String> errorMap = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);

					if ("invalid_grant".equals(errorMap.get("error")) || statusCode == HttpStatus.UNAUTHORIZED) {
						throw new AuthenticationException(errorMessage);
					}
				} catch (JsonProcessingException | IllegalArgumentException ignored) {
					if (statusCode == HttpStatus.UNAUTHORIZED) {
						throw new AuthenticationException(errorMessage);
					}
				}
				throw new AuthenticationException(errorMessage);

			} else {
				// Throw a general runtime error for other unhandled 4xx statuses (e.g., 404,
				// 403)
				throw new RuntimeException(
						"Keycloak connection failed. Status: " + statusCode + ". Detail: " + e.getStatusText(), e);
			}
		}
	}

	public String getAdminToken() {
		String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", "lending-service-admin");
		formData.add("client_secret", "6vhMkSc6tnuLvBYtJSAu6EcWy5Z6D9wl");
		formData.add("grant_type", "client_credentials");
//		formData.add("username", "admin");
//		formData.add("password", "admin");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
		Map<String, Object> responseBody;
		if (response.getStatusCode().is2xxSuccessful()) {
			// Safe to return the map if the response is successful
			responseBody = response.getBody();
		} else {
			// Provide better error logging/handling
			throw new RuntimeException("Failed to get token from Keycloak. Status: " + response.getStatusCode());
		}

		if (responseBody != null && responseBody.containsKey("access_token")) {

			// 2. Access the "access_token" key and cast its value to a String
			String accessToken = (String) responseBody.get("access_token");

			return accessToken;
		}

		// Handle the case where the token is missing or the body is empty
		throw new RuntimeException("Keycloak response body is missing or 'access_token' not found.");
	}

	public String getStudentRoleId(String adminToken, String role) {
		String roleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + role;
		RestTemplate restTemplate = new RestTemplate();
		// 1. Setup headers with Bearer Auth
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(adminToken);
		HttpEntity<String> entity = new HttpEntity<>(headers);

		try {
			// 2. Call the GET endpoint. Keycloak returns a RoleRepresentation (Map in this
			// case).
			ResponseEntity<Map> response = restTemplate.exchange(roleUrl, HttpMethod.GET, entity, Map.class);

			// 3. Check for success and extract the ID
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				// The Keycloak API returns a JSON object with the role details, including the
				// 'id' field.
				String roleId = (String) response.getBody().get("id");
				if (roleId == null) {
					throw new RuntimeException("Keycloak role response is missing the 'id' field.");
				}
				return roleId;
			} else {
				throw new RuntimeException(
						"Failed to get 'student' role from Keycloak. Status: " + response.getStatusCode());
			}

		} catch (HttpClientErrorException e) {
			// 4. Handle 404 Not Found specifically (Role does not exist)
			if (e.getStatusCode().value() == 404) {
				throw new RuntimeException("Keycloak role 'student' not found in realm '" + realm + "'. "
						+ "Please create this role in the Keycloak Admin Console first.", e);
			}
			// Re-throw other HTTP client errors (401, 403, 400, etc.)
			throw new RuntimeException("Keycloak Admin API error during role lookup: " + e.getMessage(), e);
		}
	}

	// Inside KeycloakAuthService
	public void createUserAndAssignDefaultRole(RegisterRequest request) {
		String adminToken = getAdminToken();
		String studentRoleId = getStudentRoleId(adminToken, request.role()); // Get the role ID once
		String userCreationUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(adminToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		// 1. Create the Credentials object (Password)
		Map<String, Object> credential = new HashMap<>();
		credential.put("type", "password");
		credential.put("value", request.password()); // Use the password from the request
		credential.put("temporary", false);

		// 2. Create the main User Representation map
		Map<String, Object> userRepresentation = new HashMap<>();

		// Core Fields
		userRepresentation.put("username", request.username());
		userRepresentation.put("email", request.email());
		userRepresentation.put("enabled", true);
		userRepresentation.put("credentials", List.of(credential));

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userRepresentation, headers);

		ResponseEntity<Void> response = new RestTemplate().exchange(userCreationUrl, HttpMethod.POST, entity,
				Void.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("Keycloak user creation failed.");
		}

		// Get the User ID from the response header (Crucial step!)
		String userLocation = response.getHeaders().getLocation().toString();
		String userId = userLocation.substring(userLocation.lastIndexOf('/') + 1);

		// 2. ASSIGN ROLE: POST to /users/{userId}/role-mappings/realm
		String roleMappingUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId
				+ "/role-mappings/realm";

		// Keycloak expects a list containing the role object
		List<Map<String, String>> roleMappingBody = List.of(Map.of("id", studentRoleId, "name", request.role()));

		HttpHeaders headers_role = new HttpHeaders();
		headers_role.setBearerAuth(adminToken);
		headers_role.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<List<Map<String, String>>> roleEntity = new HttpEntity<>(roleMappingBody, headers_role);

		new RestTemplate().exchange(roleMappingUrl, HttpMethod.POST, roleEntity, Void.class);
		// You must handle any failure here too!
	}

	// KeycloakUserService.java

	/**
	 * Exchanges an expired Refresh Token for a new Access Token from Keycloak.
	 */
	public Map<String, Object> refreshToken(String refreshToken) {
		String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", clientId);
		formData.add("client_secret", clientSecret);
		formData.add("grant_type", "refresh_token");
		formData.add("refresh_token", refreshToken); // The expired refresh token

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

		try {
			ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
			return response.getBody();

		} catch (HttpClientErrorException e) {
			// Keycloak returns 400 Bad Request if the refresh token is expired or invalid.
			if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				throw new AuthenticationException("Invalid or expired refresh token. Please log in again.");
			}
			throw new RuntimeException("Failed to refresh token from Keycloak. Status: " + e.getStatusCode(), e);
		}
	}

}
