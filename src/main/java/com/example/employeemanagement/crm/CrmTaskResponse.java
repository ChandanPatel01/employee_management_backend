package com.example.employeemanagement.crm;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record CrmTaskResponse(
		Long id,
		Long customerId,
		String customerName,
		String title,
		String description,
		LocalDate followUpDate,
		LocalTime followUpTime,
		String priority,
		String status,
		String assignedTo,
		String reminderType,
		Instant createdAt,
		Instant updatedAt
) {
	public static CrmTaskResponse from(CrmTask task) {
		Customer customer = task == null ? null : task.getCustomer();
		return new CrmTaskResponse(
				task == null ? null : task.getId(),
				customer == null ? null : customer.getId(),
				customer == null ? null : customer.getCompanyName(),
				task == null ? null : task.getTitle(),
				task == null ? null : task.getDescription(),
				task == null ? null : task.getFollowUpDate(),
				task == null ? null : task.getFollowUpTime(),
				task == null ? null : task.getPriority(),
				task == null ? null : task.getStatus(),
				task == null ? null : task.getAssignedTo(),
				task == null ? null : task.getReminderType(),
				task == null ? null : task.getCreatedAt(),
				task == null ? null : task.getUpdatedAt());
	}
}
