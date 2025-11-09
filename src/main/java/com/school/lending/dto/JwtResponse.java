package com.school.lending.dto;

public record JwtResponse(String token, String type) {
	/**
	 * Compact constructor to simplify instantiation, defaulting the token type to
	 * "Bearer".
	 * 
	 * @param token The access token string.
	 */
	public JwtResponse(String token) {
		this(token, "Bearer");
	}
}
