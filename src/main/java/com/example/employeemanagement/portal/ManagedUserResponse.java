package com.example.employeemanagement.portal;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.UserRole;

import java.time.Instant;

public record ManagedUserResponse(
		Long id,
		String name,
		String email,
		UserRole role,
		Instant createdAt
) {

	public static ManagedUserResponse from(AppUser user) {
		return new ManagedUserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedAt());
	}
}
