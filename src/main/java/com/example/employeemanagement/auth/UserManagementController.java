package com.example.employeemanagement.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	public ManagedUserResponse createUser(@Valid @RequestBody CreateUserRequest request, HttpServletRequest servletRequest) {
		return userManagementService.createUser(request, authenticatedUserId(servletRequest));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}

		throw new InvalidCredentialsException();
	}
}
