package com.example.employeemanagement.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TaskCreateRequest(
		@NotBlank
		String title,
		String description,
		@NotNull
		Long assignedToEmployeeId,
		TaskPriority priority,
		LocalDate deadline
) {
}
