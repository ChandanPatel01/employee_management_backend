package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.employee.Employee;

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
		Employee employee = update == null ? null : update.getEmployee();
		AppUser reviewer = update == null ? null : update.getReviewedBy();
		return new DailyUpdateResponse(
				update == null ? null : update.getId(),
				employee == null ? null : employee.getId(),
				employee == null ? null : employee.getEmployeeCode(),
				fullName(employee),
				employee == null ? null : employee.getDepartment(),
				update == null ? null : update.getUpdateText(),
				update == null ? null : update.getBlockers(),
				update == null ? null : update.getWorkDate(),
				update == null ? null : update.getStatus(),
				update == null ? null : update.getManagerComment(),
				reviewer == null ? null : reviewer.getId(),
				reviewer == null ? null : reviewer.getName(),
				update == null ? null : update.getReviewedAt(),
				update == null ? null : update.getCreatedAt());
	}

	private static String fullName(Employee employee) {
		if (employee == null) {
			return null;
		}
		return ("%s %s".formatted(
				employee.getFirstName() == null ? "" : employee.getFirstName(),
				employee.getLastName() == null ? "" : employee.getLastName())).trim();
	}
}
