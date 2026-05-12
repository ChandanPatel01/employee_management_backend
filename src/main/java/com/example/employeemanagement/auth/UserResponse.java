package com.example.employeemanagement.auth;

public record UserResponse(
		Long id,
		String name,
		String email
) {

	public static UserResponse from(AppUser user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail());
	}
}
