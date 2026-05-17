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
				notification == null ? null : notification.getId(),
				notification == null ? null : notification.getTitle(),
				notification == null ? null : notification.getMessage(),
				notification == null ? null : notification.getType(),
				notification != null && notification.isReadFlag(),
				notification == null ? null : notification.getCreatedAt());
	}
}
