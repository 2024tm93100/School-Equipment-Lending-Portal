package com.school.lending.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(@NotBlank(message = "Refresh token cannot be empty") String refreshToken) {
}