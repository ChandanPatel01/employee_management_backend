package com.example.employeemanagement.auth;

import com.example.employeemanagement.employee.Employee;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateUserResponse(
		Long id,
		String name,
		String email,
		UserRole role,
		Long employeeId,
		String employeeCode,
		String employeeName,
		String department,
		String designation,
		String phone,
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
		Employee employee = user.getEmployee();
		return new CreateUserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				employee == null ? null : employee.getId(),
				employee == null ? null : employee.getEmployeeCode(),
				employee == null ? null : employee.getFirstName() + " " + employee.getLastName(),
				employee == null ? null : employee.getDepartment(),
				employee == null ? null : employee.getJobTitle(),
				employee == null ? null : employee.getPhone(),
				user.getCreatedBy(),
				user.getCreatedAt(),
				includePasswordTracking ? user.isForcePasswordChange() : null,
				includePasswordTracking ? user.isPasswordChanged() : null,
				includePasswordTracking ? user.getPasswordChangedAt() : null,
				onboardingEmailSent,
				message);
	}
}
