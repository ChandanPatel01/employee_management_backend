package com.example.employeemanagement.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(
		@NotBlank
		@Size(min = 8, message = "Temporary password must be at least 8 characters")
		String temporaryPassword
) {
}
