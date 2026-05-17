package com.example.employeemanagement.employee;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.workflow.WorkflowException;
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
	private final AppUserRepository appUserRepository;

	public EmployeeService(EmployeeRepository employeeRepository, AppUserRepository appUserRepository) {
		this.employeeRepository = employeeRepository;
		this.appUserRepository = appUserRepository;
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department) {
		if (department == null || department.isBlank()) {
			return employeeRepository.findAll();
		}

		return employeeRepository.findByDepartmentIgnoreCase(department);
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department, long actorId) {
		AppUser actor = actor(actorId);
		if (hasPeopleAccess(actor)) {
			return getEmployees(department);
		}

		Employee employee = requireEmployee(actor);
		if (actor.getRole() == UserRole.MANAGER) {
			String managerDepartment = department == null || department.isBlank() ? employee.getDepartment() : department;
			if (employee.getDepartment() == null || managerDepartment == null || !employee.getDepartment().equalsIgnoreCase(managerDepartment)) {
				throw new WorkflowException("Managers can view only their team.");
			}
			return employeeRepository.findByDepartmentIgnoreCase(managerDepartment);
		}

		return List.of(employee);
	}

	@Transactional(readOnly = true)
	public Employee getEmployee(Long id) {
		return employeeRepository.findById(id)
				.orElseThrow(() -> new EmployeeNotFoundException(id));
	}

	@Transactional(readOnly = true)
	public Employee getEmployee(Long id, long actorId) {
		AppUser actor = actor(actorId);
		Employee employee = getEmployee(id);
		if (hasPeopleAccess(actor)) {
			return employee;
		}
		Employee actorEmployee = requireEmployee(actor);
		if (actor.getRole() == UserRole.MANAGER && sameDepartment(actorEmployee, employee)) {
			return employee;
		}
		if (actorEmployee.getId().equals(employee.getId())) {
			return employee;
		}
		throw new WorkflowException("You can view only permitted employee profiles.");
	}

	public Employee createEmployee(Employee employee) {
		if (employeeRepository.existsByEmail(employee.getEmail())) {
			throw new DuplicateEmployeeEmailException(employee.getEmail());
		}

		employee.setId(null);
		employee.setEmployeeCode(generateEmployeeCode(employee));
		return employeeRepository.save(employee);
	}

	public Employee createEmployee(Employee employee, long actorId) {
		ensurePeopleAdmin(actor(actorId));
		return createEmployee(employee);
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
		employee.setPhone(updatedEmployee.getPhone());
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

	public Employee updateEmployee(Long id, Employee updatedEmployee, long actorId) {
		AppUser actor = actor(actorId);
		if (hasPeopleAccess(actor)) {
			return updateEmployee(id, updatedEmployee);
		}

		Employee actorEmployee = requireEmployee(actor);
		if (!actorEmployee.getId().equals(id)) {
			throw new WorkflowException("You can update only your own employee profile.");
		}

		Employee employee = getEmployee(id);
		employee.setPhone(updatedEmployee.getPhone());
		employee.setDateOfBirth(updatedEmployee.getDateOfBirth());
		employee.setGender(updatedEmployee.getGender());
		employee.setMaritalStatus(updatedEmployee.getMaritalStatus());
		employee.setPhotoUrl(updatedEmployee.getPhotoUrl());
		return employeeRepository.save(employee);
	}

	public void deleteEmployee(Long id) {
		if (!employeeRepository.existsById(id)) {
			throw new EmployeeNotFoundException(id);
		}

		employeeRepository.deleteById(id);
	}

	public void deleteEmployee(Long id, long actorId) {
		ensurePeopleAdmin(actor(actorId));
		deleteEmployee(id);
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

	private AppUser actor(long actorId) {
		return appUserRepository.findById(actorId)
				.orElseThrow(() -> new WorkflowException("Authenticated user was not found."));
	}

	private Employee requireEmployee(AppUser user) {
		if (user.getEmployee() == null) {
			throw new WorkflowException("Your login account is not linked to an employee profile.");
		}
		return user.getEmployee();
	}

	private boolean hasPeopleAccess(AppUser user) {
		return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FOUNDER || user.getRole() == UserRole.HR;
	}

	private void ensurePeopleAdmin(AppUser user) {
		if (hasPeopleAccess(user)) {
			return;
		}
		throw new WorkflowException("You do not have permission to manage employees.");
	}

	private boolean sameDepartment(Employee left, Employee right) {
		return left.getDepartment() != null && right.getDepartment() != null
				&& left.getDepartment().equalsIgnoreCase(right.getDepartment());
	}
}
