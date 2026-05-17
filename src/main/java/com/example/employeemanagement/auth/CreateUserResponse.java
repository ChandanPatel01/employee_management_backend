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
		Employee employee = user == null ? null : user.getEmployee();
		return new CreateUserResponse(
				user == null ? null : user.getId(),
				user == null ? null : user.getName(),
				user == null ? null : user.getEmail(),
				user == null ? null : user.getRole(),
				employee == null ? null : employee.getId(),
				employee == null ? null : employee.getEmployeeCode(),
				fullName(employee),
				employee == null ? null : employee.getDepartment(),
				employee == null ? null : employee.getJobTitle(),
				employee == null ? null : employee.getPhone(),
				user == null ? null : user.getCreatedBy(),
				user == null ? null : user.getCreatedAt(),
				includePasswordTracking && user != null ? user.isForcePasswordChange() : null,
				includePasswordTracking && user != null ? user.isPasswordChanged() : null,
				includePasswordTracking && user != null ? user.getPasswordChangedAt() : null,
				onboardingEmailSent,
				message);
	}

	private static String fullName(Employee employee) {
		if (employee == null) {
			return null;
		}
		return ("%s %s".formatted(
				employee.getFirstName() == null ? "" : employee.getFirstName(),
				employee.getLastName() == null ? "" : employee.getLastName())).trim();
	}
}
