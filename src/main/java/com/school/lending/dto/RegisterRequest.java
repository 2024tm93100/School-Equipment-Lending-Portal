package com.school.lending.dto;

public record RegisterRequest(String firstName, String lastName, String email, String role, String password) {
}
