package com.example.employeemanagement.auth;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateUserResponse(
		Long id,
		String name,
		String email,
		UserRole role,
		String createdBy,
		Instant createdAt,
		Boolean forcePasswordChange,
		Boolean passwordChanged,
		Instant passwordChangedAt,
		boolean onboardingEmailSent,
		String message
) {

	public static CreateUserResponse from(
			AppUser user,
			boolean includePasswordTracking,
			boolean onboardingEmailSent,
			String message) {
		return new CreateUserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.getCreatedBy(),
				user.getCreatedAt(),
				includePasswordTracking ? user.isForcePasswordChange() : null,
				includePasswordTracking ? user.isPasswordChanged() : null,
				includePasswordTracking ? user.getPasswordChangedAt() : null,
				onboardingEmailSent,
				message);
	}
}
