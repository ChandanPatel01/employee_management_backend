package com.example.employeemanagement.employee;

public record EmployeeDeactivationResponse(
		boolean success,
		String message,
		EmployeeResponse employee
) {
}
