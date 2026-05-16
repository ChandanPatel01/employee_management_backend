package com.example.employeemanagement.portal;

import com.example.employeemanagement.auth.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateRequest(
		@NotNull
		UserRole role
) {
}
