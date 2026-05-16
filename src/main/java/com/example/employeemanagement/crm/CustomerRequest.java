package com.example.employeemanagement.crm;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
		@NotBlank
		String companyName,

		@NotBlank
		String contactPerson,

		@NotBlank
		@Email
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
		String notes
) {
}
