package com.example.employeemanagement.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

	private final UserManagementService userManagementService;

	public UserManagementController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	@GetMapping
	public List<ManagedUserResponse> getUsers(HttpServletRequest request) {
		return userManagementService.getUsers(authenticatedUserId(request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest request, HttpServletRequest servletRequest) {
		return userManagementService.createUser(request, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/block")
	public ManagedUserResponse blockUser(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
		return userManagementService.setUserBlocked(id, true, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/unblock")
	public ManagedUserResponse unblockUser(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
		return userManagementService.setUserBlocked(id, false, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/reset-password")
	public ManagedUserResponse resetUserPassword(
			@PathVariable("id") Long id,
			@Valid @RequestBody ResetUserPasswordRequest request,
			HttpServletRequest servletRequest) {
		return userManagementService.resetUserPassword(id, request.temporaryPassword(), authenticatedUserId(servletRequest));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}

		throw new InvalidCredentialsException();
	}
}
