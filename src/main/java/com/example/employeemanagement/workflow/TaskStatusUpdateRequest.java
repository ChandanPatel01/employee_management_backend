package com.example.employeemanagement.workflow;

import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequest(
		@NotNull
		TaskStatus status,
		String progressNote
) {
}
