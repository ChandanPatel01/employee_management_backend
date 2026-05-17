package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.employee.EmployeeResponse;

import java.time.Instant;
import java.time.LocalDate;

public record LeaveResponse(
		Long id,
		EmployeeResponse employee,
		String leaveType,
		LocalDate fromDate,
		LocalDate toDate,
		String description,
		LocalDate appliedDate,
		LeaveStatus status,
		String decisionReason,
		Instant decidedAt
) {

	public static LeaveResponse from(LeaveRequest leaveRequest) {
		if (leaveRequest == null) {
			return null;
		}

		return new LeaveResponse(
				leaveRequest.getId(),
				EmployeeResponse.from(leaveRequest.getEmployee()),
				leaveRequest.getLeaveType(),
				leaveRequest.getFromDate(),
				leaveRequest.getToDate(),
				leaveRequest.getDescription(),
				leaveRequest.getAppliedDate(),
				leaveRequest.getStatus(),
				leaveRequest.getDecisionReason(),
				leaveRequest.getDecidedAt());
	}
}
