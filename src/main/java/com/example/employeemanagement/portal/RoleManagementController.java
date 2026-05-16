package com.example.employeemanagement.portal;

import com.example.employeemanagement.auth.UserRole;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class RoleManagementController {

	private final RoleManagementService roleManagementService;

	public RoleManagementController(RoleManagementService roleManagementService) {
		this.roleManagementService = roleManagementService;
	}

	@GetMapping("/roles")
	public List<UserRole> getRoles() {
		return roleManagementService.getRoles();
	}

	@GetMapping("/users")
	public List<ManagedUserResponse> getUsers() {
		return roleManagementService.getUsers();
	}

	@PutMapping("/users/{id}/role")
	public ManagedUserResponse updateRole(
			@PathVariable("id") Long id,
			@Valid @RequestBody UserRoleUpdateRequest request) {
		return roleManagementService.updateRole(id, request.role());
	}
}
