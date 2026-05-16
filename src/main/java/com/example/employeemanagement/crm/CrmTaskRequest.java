package com.example.employeemanagement.crm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CrmTaskRequest(
		@NotNull
		Long customerId,

		@NotBlank
		String title,

		String description,

		@NotNull
		LocalDate followUpDate,

		@NotNull
		LocalTime followUpTime,

		@NotBlank
		String priority,

		@NotBlank
		String status,

		String assignedTo,
		String reminderType
) {
}
