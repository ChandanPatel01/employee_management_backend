package com.example.employeemanagement.timeoff;

import jakarta.validation.constraints.NotNull;

public record LeaveDecisionRequest(
		@NotNull
		LeaveStatus status,

		String reason
) {
}
