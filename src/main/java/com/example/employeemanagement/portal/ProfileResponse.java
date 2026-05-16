package com.example.employeemanagement.portal;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.employee.Employee;

public record ProfileResponse(
		Long userId,
		String name,
		String email,
		UserRole role,
		Long employeeId,
		String employeeCode,
		String department,
		String jobTitle,
		String photoUrl
) {

	public static ProfileResponse from(AppUser user, Employee employee) {
		return new ProfileResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				employee == null ? null : employee.getId(),
				employee == null ? null : employee.getEmployeeCode(),
				employee == null ? null : employee.getDepartment(),
				employee == null ? null : employee.getJobTitle(),
				employee == null ? null : employee.getPhotoUrl());
	}
}
