package com.example.employeemanagement.crm;

public class CrmResourceNotFoundException extends RuntimeException {

	public CrmResourceNotFoundException(String resource, Long id) {
		super(resource + " with id " + id + " was not found");
	}
}
