package com.example.employeemanagement.portal;

public class PortalResourceNotFoundException extends RuntimeException {

	public PortalResourceNotFoundException(String resourceName, Long id) {
		super(resourceName + " not found with id " + id);
	}

	public PortalResourceNotFoundException(String message) {
		super(message);
	}
}
