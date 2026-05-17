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
				customer == null ? null : customer.getId(),
				customer == null ? null : customer.getCompanyName(),
				customer == null ? null : customer.getContactPerson(),
				customer == null ? null : customer.getEmail(),
				customer == null ? null : customer.getPhone(),
				customer == null ? null : customer.getAddress(),
				customer == null ? null : customer.getProjectName(),
				customer == null ? null : customer.getProjectStatus(),
				customer == null ? null : customer.getPaymentStatus(),
				customer == null ? null : customer.getPaymentHistory(),
				customer == null ? null : customer.getAssignedTeam(),
				customer == null ? null : customer.getSupportTickets(),
				customer == null ? null : customer.getPreviousCommunication(),
				customer == null ? null : customer.getNotes(),
				customer == null ? null : customer.getCreatedAt(),
				customer == null ? null : customer.getUpdatedAt());
	}
}
