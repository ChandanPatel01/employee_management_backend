package com.example.employeemanagement.timeoff;

public record LeaveDecisionResponse(
		LeaveRequest leave,
		boolean statusUpdated,
		boolean emailSent,
		String message
) {
}
