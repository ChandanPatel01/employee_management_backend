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
		Customer customer = task.getCustomer();
		return new CrmTaskResponse(
				task.getId(),
				customer.getId(),
				customer.getCompanyName(),
				task.getTitle(),
				task.getDescription(),
				task.getFollowUpDate(),
				task.getFollowUpTime(),
				task.getPriority(),
				task.getStatus(),
				task.getAssignedTo(),
				task.getReminderType(),
				task.getCreatedAt(),
				task.getUpdatedAt());
	}
}
