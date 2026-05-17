package com.example.employeemanagement.auth;

import com.example.employeemanagement.employee.EmploymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateUserRequest(
		Long employeeId,

		@Valid
		EmployeeProfileRequest employee,

		String name,

		@Email
		String email,

		@Size(min = 8, message = "Temporary password must be at least 8 characters")
		String temporaryPassword,

		@NotNull
		UserRole role
) {

	public record EmployeeProfileRequest(
			String firstName,
			String lastName,

			@Email
			String email,

			String department,
			String jobTitle,
			String phone,

			@PositiveOrZero
			BigDecimal salary,

			LocalDate hireDate,
			EmploymentStatus status,
			String photoUrl
	) {
	}
}
