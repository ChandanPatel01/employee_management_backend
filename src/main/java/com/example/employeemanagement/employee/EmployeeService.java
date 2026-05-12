package com.example.employeemanagement.employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class EmployeeService {

	private final SecureRandom secureRandom = new SecureRandom();
	private final EmployeeRepository employeeRepository;

	public EmployeeService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department) {
		if (department == null || department.isBlank()) {
			return employeeRepository.findAll();
		}

		return employeeRepository.findByDepartmentIgnoreCase(department);
	}

	@Transactional(readOnly = true)
	public Employee getEmployee(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new EmployeeNotFoundException(id));
	}

	public Employee createEmployee(Employee employee) {
		if (employeeRepository.existsByEmail(employee.getEmail())) {
			throw new DuplicateEmployeeEmailException(employee.getEmail());
		}

		employee.setId(null);
		employee.setEmployeeCode(generateEmployeeCode(employee));
		return employeeRepository.save(employee);
	}

	public Employee updateEmployee(Long id, Employee updatedEmployee) {
		Employee employee = getEmployee(id);
		if (employeeRepository.existsByEmailAndIdNot(updatedEmployee.getEmail(), id)) {
			throw new DuplicateEmployeeEmailException(updatedEmployee.getEmail());
		}

		employee.setFirstName(updatedEmployee.getFirstName());
		employee.setLastName(updatedEmployee.getLastName());
		employee.setEmail(updatedEmployee.getEmail());
		employee.setDepartment(updatedEmployee.getDepartment());
		employee.setJobTitle(updatedEmployee.getJobTitle());
		employee.setSalary(updatedEmployee.getSalary());
		employee.setHireDate(updatedEmployee.getHireDate());
		employee.setDateOfBirth(updatedEmployee.getDateOfBirth());
		employee.setGender(updatedEmployee.getGender());
		employee.setMaritalStatus(updatedEmployee.getMaritalStatus());
		employee.setPhotoUrl(updatedEmployee.getPhotoUrl());
		employee.setStatus(updatedEmployee.getStatus());
		if (employee.getEmployeeCode() == null || employee.getEmployeeCode().isBlank()) {
			employee.setEmployeeCode(generateEmployeeCode(employee));
		}

		return employeeRepository.save(employee);
	}

	public void deleteEmployee(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new EmployeeNotFoundException(id);
		}

		employeeRepository.deleteById(id);
	}

	private String generateEmployeeCode(Employee employee) {
		String prefix = initials(employee.getFirstName(), employee.getLastName());
		String employeeCode;

		do {
			employeeCode = prefix + String.format("%04d", secureRandom.nextInt(10_000));
		} while (employeeRepository.existsByEmployeeCode(employeeCode));

		return employeeCode;
	}

	private String initials(String firstName, String lastName) {
		String first = firstName == null || firstName.isBlank() ? "E" : firstName.trim().substring(0, 1);
		String last = lastName == null || lastName.isBlank() ? "M" : lastName.trim().substring(0, 1);
		return (first + last).toUpperCase(Locale.ROOT);
	}
}
