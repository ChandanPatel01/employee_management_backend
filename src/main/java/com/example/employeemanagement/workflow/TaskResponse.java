package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.employee.Employee;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
		Long id,
		String title,
		String description,
		Long assignedToEmployeeId,
		String assignedToEmployeeCode,
		String assignedToName,
		String assignedToDepartment,
		Long assignedByUserId,
		String assignedByName,
		TaskPriority priority,
		TaskStatus status,
		String progressNote,
		String managerComment,
		LocalDate deadline,
		Instant createdAt,
		Instant updatedAt
) {

	public static TaskResponse from(WorkTask task) {
		Employee assignee = task == null ? null : task.getAssignedToEmployee();
		AppUser assigner = task == null ? null : task.getAssignedByUser();
		return new TaskResponse(
				task == null ? null : task.getId(),
				task == null ? null : task.getTitle(),
				task == null ? null : task.getDescription(),
				assignee == null ? null : assignee.getId(),
				assignee == null ? null : assignee.getEmployeeCode(),
				fullName(assignee),
				assignee == null ? null : assignee.getDepartment(),
				assigner == null ? null : assigner.getId(),
				assigner == null ? null : assigner.getName(),
				task == null ? null : task.getPriority(),
				task == null ? null : task.getStatus(),
				task == null ? null : task.getProgressNote(),
				task == null ? null : task.getManagerComment(),
				task == null ? null : task.getDeadline(),
				task == null ? null : task.getCreatedAt(),
				task == null ? null : task.getUpdatedAt());
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
