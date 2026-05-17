package com.example.employeemanagement.workflow;

import java.time.LocalDate;

public record TaskCommentRequest(
		String managerComment,
		TaskPriority priority,
		LocalDate deadline,
		TaskStatus status
) {
}
