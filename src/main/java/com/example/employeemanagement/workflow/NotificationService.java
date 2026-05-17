package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

	private final UserNotificationRepository notificationRepository;
	private final AppUserRepository appUserRepository;

	public NotificationService(UserNotificationRepository notificationRepository, AppUserRepository appUserRepository) {
		this.notificationRepository = notificationRepository;
		this.appUserRepository = appUserRepository;
	}

	@Transactional(readOnly = true)
	public List<NotificationResponse> getMyNotifications(long userId) {
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(NotificationResponse::from)
				.toList();
	}

	public NotificationResponse markRead(Long id, long userId) {
		UserNotification notification = notificationRepository.findById(id)
				.orElseThrow(() -> new WorkflowException("Notification not found."));
		Long notificationUserId = notification.getUser() == null ? null : notification.getUser().getId();
		if (notificationUserId == null || !notificationUserId.equals(userId)) {
			throw new WorkflowException("You can update only your notifications.");
		}

		notification.setReadFlag(true);
		return NotificationResponse.from(notificationRepository.save(notification));
	}

	public void markAllRead(long userId) {
		List<UserNotification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
		notifications.forEach((notification) -> notification.setReadFlag(true));
		notificationRepository.saveAll(notifications);
	}

	public void notifyUser(AppUser user, String title, String message, NotificationType type) {
		if (user == null) {
			return;
		}

		UserNotification notification = new UserNotification();
		notification.setUser(user);
		notification.setTitle(cleanOrDefault(title, "Notification"));
		notification.setMessage(cleanOrDefault(message, ""));
		notification.setType(type);
		notificationRepository.save(notification);
	}

	public void notifyEmployee(Long employeeId, String title, String message, NotificationType type) {
		if (employeeId == null) {
			return;
		}

		appUserRepository.findByEmployeeId(employeeId)
				.ifPresent((user) -> notifyUser(user, title, message, type));
	}

	private String cleanOrDefault(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value.trim();
	}
}
