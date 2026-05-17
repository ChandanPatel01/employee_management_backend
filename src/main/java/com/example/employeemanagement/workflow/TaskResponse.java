package com.example.employeemanagement.workflow;

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
		return new TaskResponse(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.getAssignedToEmployee().getId(),
				task.getAssignedToEmployee().getEmployeeCode(),
				task.getAssignedToEmployee().getFirstName() + " " + task.getAssignedToEmployee().getLastName(),
				task.getAssignedToEmployee().getDepartment(),
				task.getAssignedByUser().getId(),
				task.getAssignedByUser().getName(),
				task.getPriority(),
				task.getStatus(),
				task.getProgressNote(),
				task.getManagerComment(),
				task.getDeadline(),
				task.getCreatedAt(),
				task.getUpdatedAt());
	}
}
