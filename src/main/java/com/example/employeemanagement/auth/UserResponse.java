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
		Employee employee = user == null ? null : user.getEmployee();
		return new UserResponse(
				user == null ? null : user.getId(),
				user == null ? null : user.getName(),
				user == null ? null : user.getEmail(),
				user == null ? null : user.getRole(),
				user != null && user.isForcePasswordChange(),
				employee == null ? null : employee.getId(),
				employee == null ? null : employee.getEmployeeCode(),
				employee == null ? null : employee.getDepartment(),
				employee == null ? null : employee.getJobTitle(),
				employee == null ? null : employee.getPhone(),
				employee == null ? null : employee.getPhotoUrl());
	}
}
