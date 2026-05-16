package com.example.employeemanagement.portal;

public record PerformanceSummaryResponse(
		String employeeEmail,
		long totalTasks,
		long completedTasks,
		long submittedUpdates,
		double completionRate
) {
}
