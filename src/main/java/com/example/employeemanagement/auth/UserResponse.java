package com.example.employeemanagement.auth;

import com.example.employeemanagement.employee.Employee;

public record UserResponse(
		Long id,
		String name,
		String email,
		UserRole role,
		boolean forcePasswordChange,
		Long employeeId,
		String employeeCode,
		String department,
		String designation,
		String phone,
		String photoUrl
) {

	public static UserResponse from(AppUser user) {
		Employee employee = user.getEmployee();
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getRole(),
				user.isForcePasswordChange(),
				employee == null ? null : employee.getId(),
				employee == null ? null : employee.getEmployeeCode(),
				employee == null ? null : employee.getDepartment(),
				employee == null ? null : employee.getJobTitle(),
				employee == null ? null : employee.getPhone(),
				employee == null ? null : employee.getPhotoUrl());
	}
}
