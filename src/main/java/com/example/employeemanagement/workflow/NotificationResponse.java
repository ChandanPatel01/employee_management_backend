package com.example.employeemanagement.workflow;

import java.time.Instant;

public record NotificationResponse(
		Long id,
		String title,
		String message,
		NotificationType type,
		boolean read,
		Instant createdAt
) {

	public static NotificationResponse from(UserNotification notification) {
		return new NotificationResponse(
				notification.getId(),
				notification.getTitle(),
				notification.getMessage(),
				notification.getType(),
				notification.isRead(),
				notification.getCreatedAt());
	}
}
