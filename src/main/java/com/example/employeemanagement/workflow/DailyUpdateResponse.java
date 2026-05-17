package com.example.employeemanagement.workflow;

import java.time.Instant;
import java.time.LocalDate;

public record DailyUpdateResponse(
		Long id,
		Long employeeId,
		String employeeCode,
		String employeeName,
		String department,
		String updateText,
		String blockers,
		LocalDate workDate,
		DailyUpdateStatus status,
		String managerComment,
		Long reviewedBy,
		String reviewedByName,
		Instant reviewedAt,
		Instant createdAt
) {

	public static DailyUpdateResponse from(DailyWorkUpdate update) {
		return new DailyUpdateResponse(
				update.getId(),
				update.getEmployee().getId(),
				update.getEmployee().getEmployeeCode(),
				update.getEmployee().getFirstName() + " " + update.getEmployee().getLastName(),
				update.getEmployee().getDepartment(),
				update.getUpdateText(),
				update.getBlockers(),
				update.getWorkDate(),
				update.getStatus(),
				update.getManagerComment(),
				update.getReviewedBy() == null ? null : update.getReviewedBy().getId(),
				update.getReviewedBy() == null ? null : update.getReviewedBy().getName(),
				update.getReviewedAt(),
				update.getCreatedAt());
	}
}
