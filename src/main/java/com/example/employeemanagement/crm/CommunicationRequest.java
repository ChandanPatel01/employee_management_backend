package com.example.employeemanagement.crm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CommunicationRequest(
		@NotNull
		Long customerId,

		@NotNull
		CommunicationType communicationType,

		@NotBlank
		String subject,

		@NotBlank
		String summary,

		@NotNull
		LocalDateTime communicationDate,

		String nextAction,
		String createdBy
) {
}
