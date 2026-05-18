package com.example.employeemanagement.auth;

public class DuplicateUserEmailException extends RuntimeException {

	public DuplicateUserEmailException(String email) {
		super("Account email already exists: " + email);
	}

	public static DuplicateUserEmailException inactiveAccount() {
		return new DuplicateUserEmailException();
	}

	private DuplicateUserEmailException() {
		super("This user account is inactive. Please reactivate instead of creating duplicate.");
	}
}
