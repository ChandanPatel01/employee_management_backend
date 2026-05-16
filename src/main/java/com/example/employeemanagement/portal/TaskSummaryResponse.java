package com.example.employeemanagement.portal;

public record TaskSummaryResponse(
		long total,
		long pending,
		long inProgress,
		long completed,
		long highPriority
) {
}
