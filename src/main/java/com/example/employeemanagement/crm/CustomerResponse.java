package com.example.employeemanagement.crm;

import java.time.Instant;

public record CustomerResponse(
		Long id,
		String companyName,
		String contactPerson,
		String email,
		String phone,
		String address,
		String projectName,
		String projectStatus,
		String paymentStatus,
		String paymentHistory,
		String assignedTeam,
		String supportTickets,
		String previousCommunication,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
	public static CustomerResponse from(Customer customer) {
		return new CustomerResponse(
				customer.getId(),
				customer.getCompanyName(),
				customer.getContactPerson(),
				customer.getEmail(),
				customer.getPhone(),
				customer.getAddress(),
				customer.getProjectName(),
				customer.getProjectStatus(),
				customer.getPaymentStatus(),
				customer.getPaymentHistory(),
				customer.getAssignedTeam(),
				customer.getSupportTickets(),
				customer.getPreviousCommunication(),
				customer.getNotes(),
				customer.getCreatedAt(),
				customer.getUpdatedAt());
	}
}
