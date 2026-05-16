package com.example.employeemanagement.auth;

public record AuthResponse(
		String token,
		String tokenType,
		long expiresIn,
		UserResponse user,
		UserRole role,
		String name,
		boolean forcePasswordChange
) {
}
