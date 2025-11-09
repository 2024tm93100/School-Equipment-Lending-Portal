package com.school.lending.dto;

import com.school.lending.model.Role;

public record UserResponseDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    Role role
) {}
