package com.school.lending.dto;

public record TokenRefreshResponse(String accessToken, String refreshToken, String tokenType, Long expiresIn,
		Long refreshExpiresIn) {
	/**
	 * Compact constructor that defaults the tokenType to "Bearer".
	 */
	public TokenRefreshResponse(String accessToken, String refreshToken, Long expiresIn, Long refreshExpiresIn) {
		this(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn);
	}
}