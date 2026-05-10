package com.example.employeemanagement.employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

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
		employee.setStatus(updatedEmployee.getStatus());

		return employeeRepository.save(employee);
	}

	public void deleteEmployee(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new EmployeeNotFoundException(id);
		}

		employeeRepository.deleteById(id);
	}
}
