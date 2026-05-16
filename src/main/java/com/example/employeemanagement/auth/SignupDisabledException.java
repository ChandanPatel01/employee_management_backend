package com.example.employeemanagement.auth;

public class SignupDisabledException extends RuntimeException {

	public SignupDisabledException() {
		super("Signup is disabled. Please contact the admin to create an account.");
	}
}
