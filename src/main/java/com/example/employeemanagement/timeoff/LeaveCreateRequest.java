package com.example.employeemanagement.timeoff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record LeaveCreateRequest(
		@NotNull
		Long employeeId,

		@NotBlank
		String leaveType,

		@NotNull
		LocalDate fromDate,

		@NotNull
		LocalDate toDate,

		String description
) {
}
