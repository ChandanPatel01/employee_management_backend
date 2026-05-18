package com.example.employeemanagement.auth;

public class AccountBlockedException extends RuntimeException {

	public AccountBlockedException() {
		super("Your account is inactive. Please contact admin.");
	}
}
