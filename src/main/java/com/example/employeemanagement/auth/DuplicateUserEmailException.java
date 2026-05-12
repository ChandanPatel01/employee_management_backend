package com.example.employeemanagement.auth;

public class DuplicateUserEmailException extends RuntimeException {

	public DuplicateUserEmailException(String email) {
		super("Account email already exists: " + email);
	}
}
