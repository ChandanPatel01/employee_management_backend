package com.example.employeemanagement.auth;

import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmploymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ManagedUserResponse(
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
		EmploymentStatus employeeStatus,
		String createdBy,
		Instant createdAt,
		boolean blocked,
		Boolean forcePasswordChange,
		Boolean passwordChanged,
		Instant passwordChangedAt
) {

	public static ManagedUserResponse from(AppUser user, boolean includePasswordTracking) {
		Employee employee = user == null ? null : user.getEmployee();
		return new ManagedUserResponse(
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
				employee == null ? null : employee.getStatus(),
				user == null ? null : user.getCreatedBy(),
				user == null ? null : user.getCreatedAt(),
				user != null && user.isBlocked(),
				includePasswordTracking && user != null ? user.isForcePasswordChange() : null,
				includePasswordTracking && user != null ? user.isPasswordChanged() : null,
				includePasswordTracking && user != null ? user.getPasswordChangedAt() : null);
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
