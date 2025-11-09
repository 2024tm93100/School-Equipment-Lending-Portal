package com.school.lending.exception;

/**
 * Custom exception to represent an invalid username or password after external
 * authentication (e.g., Keycloak) failure.
 */
public class AuthenticationException extends RuntimeException {
	public AuthenticationException(String message) {
		super(message);
	}
}
