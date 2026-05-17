package com.example.employeemanagement.employee;

public class EmployeeAccessDeniedException extends RuntimeException {

	public EmployeeAccessDeniedException(String message) {
		super(message);
	}
}
