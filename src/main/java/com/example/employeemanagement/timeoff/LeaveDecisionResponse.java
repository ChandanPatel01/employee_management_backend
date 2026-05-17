package com.example.employeemanagement.timeoff;

public record LeaveDecisionResponse(
		LeaveResponse leave,
		boolean statusUpdated,
		boolean emailSent,
		String message
) {
}
