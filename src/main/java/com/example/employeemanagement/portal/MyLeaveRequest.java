package com.example.employeemanagement.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MyLeaveRequest(
		@NotBlank
		String leaveType,

		@NotNull
		LocalDate fromDate,

		@NotNull
		LocalDate toDate,

		String description
) {
}
