package com.example.employeemanagement.auth;

public class AccountBlockedException extends RuntimeException {

	public AccountBlockedException() {
		super("Account is blocked. Please contact administrator.");
	}
}
