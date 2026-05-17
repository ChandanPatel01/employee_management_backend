package com.example.employeemanagement.workflow;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record DailyUpdateRequest(
		@NotBlank
		String updateText,
		String blockers,
		LocalDate workDate
) {
}
