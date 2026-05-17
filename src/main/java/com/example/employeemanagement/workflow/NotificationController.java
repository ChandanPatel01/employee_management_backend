package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping("/my")
	public List<NotificationResponse> getMyNotifications(HttpServletRequest request) {
		return notificationService.getMyNotifications(authenticatedUserId(request));
	}

	@PutMapping("/{id}/read")
	public NotificationResponse markRead(@PathVariable("id") Long id, HttpServletRequest request) {
		return notificationService.markRead(id, authenticatedUserId(request));
	}

	@PutMapping("/read-all")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markAllRead(HttpServletRequest request) {
		notificationService.markAllRead(authenticatedUserId(request));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}
		throw new InvalidCredentialsException();
	}
}
