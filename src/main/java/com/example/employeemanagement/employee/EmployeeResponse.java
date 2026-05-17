package com.example.employeemanagement.employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
		Long id,
		String employeeCode,
		String firstName,
		String lastName,
		String email,
		String department,
		String jobTitle,
		String phone,
		BigDecimal salary,
		LocalDate hireDate,
		LocalDate dateOfBirth,
		String gender,
		String maritalStatus,
		String photoUrl,
		EmploymentStatus status
) {

	public static EmployeeResponse from(Employee employee) {
		if (employee == null) {
			return null;
		}

		return new EmployeeResponse(
				employee.getId(),
				employee.getEmployeeCode(),
				employee.getFirstName(),
				employee.getLastName(),
				employee.getEmail(),
				employee.getDepartment(),
				employee.getJobTitle(),
				employee.getPhone(),
				employee.getSalary(),
				employee.getHireDate(),
				employee.getDateOfBirth(),
				employee.getGender(),
				employee.getMaritalStatus(),
				employee.getPhotoUrl(),
				employee.getStatus());
	}
}
