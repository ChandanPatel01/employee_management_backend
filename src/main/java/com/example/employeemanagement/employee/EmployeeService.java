package com.example.employeemanagement.employee;

import com.example.employeemanagement.audit.AuditLogService;
import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.workflow.NotificationService;
import com.example.employeemanagement.workflow.NotificationType;
import com.example.employeemanagement.workflow.WorkflowException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {

	private final SecureRandom secureRandom = new SecureRandom();
	private final EmployeeRepository employeeRepository;
	private final AppUserRepository appUserRepository;
	private final AuditLogService auditLogService;
	private final NotificationService notificationService;
	private final String primaryAdminEmail;

	public EmployeeService(
			EmployeeRepository employeeRepository,
			AppUserRepository appUserRepository,
			AuditLogService auditLogService,
			NotificationService notificationService,
			@Value("${admin.email:}") String primaryAdminEmail) {
		this.employeeRepository = employeeRepository;
		this.appUserRepository = appUserRepository;
		this.auditLogService = auditLogService;
		this.notificationService = notificationService;
		this.primaryAdminEmail = normalize(primaryAdminEmail);
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department) {
		return getEmployees(department, false);
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department, boolean includeInactive) {
		if (department == null || department.isBlank()) {
			return includeInactive ? employeeRepository.findAll() : employeeRepository.findByStatus(EmploymentStatus.ACTIVE);
		}

		return includeInactive
				? employeeRepository.findByDepartmentIgnoreCase(department)
				: employeeRepository.findByStatusAndDepartmentIgnoreCase(EmploymentStatus.ACTIVE, department);
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department, long actorId) {
		return getEmployees(department, false, actorId);
	}

	@Transactional(readOnly = true)
	public List<Employee> getEmployees(String department, boolean includeInactive, long actorId) {
		AppUser actor = actor(actorId);
		if (hasPeopleAccess(actor)) {
			return getEmployees(department, includeInactive);
		}

		Employee employee = requireEmployee(actor);
		if (actor.getRole() == UserRole.MANAGER) {
			String managerDepartment = department == null || department.isBlank() ? employee.getDepartment() : department;
			if (employee.getDepartment() == null || managerDepartment == null || !employee.getDepartment().equalsIgnoreCase(managerDepartment)) {
				throw new WorkflowException("Managers can view only their team.");
			}
			return employeeRepository.findByStatusAndDepartmentIgnoreCase(EmploymentStatus.ACTIVE, managerDepartment);
		}

		if (employee.getStatus() == EmploymentStatus.ACTIVE) {
			return List.of(employee);
		}

		return List.of();
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
		deactivateEmployee(id, null);
	}

	public EmployeeDeactivationResponse deleteEmployee(Long id, long actorId) {
		return deactivateEmployee(id, actor(actorId));
	}

	public EmployeeDeactivationResponse deactivateEmployee(Long id, AppUser actor) {
		if (id == null) {
			throw new EmployeeValidationException("Employee id is required.");
		}

		Employee employee = getEmployee(id);
		Optional<AppUser> linkedUser = appUserRepository.findByEmployeeId(id);
		if (actor != null) {
			ensureCanDeactivate(actor, employee, linkedUser);
		}

		if (employee.getStatus() == EmploymentStatus.INACTIVE) {
			return new EmployeeDeactivationResponse(
					true,
					"Employee is already inactive.",
					EmployeeResponse.from(employee));
		}

		employee.setStatus(EmploymentStatus.INACTIVE);
		Employee savedEmployee = employeeRepository.save(employee);
		boolean linkedUserBlocked = linkedUser
				.map((user) -> {
					user.setBlocked(true);
					appUserRepository.save(user);
					return true;
				})
				.orElse(false);

		if (actor != null) {
			auditLogService.recordEmployeeDeactivated(actor, savedEmployee, linkedUserBlocked);
			notificationService.notifyUser(
					actor,
					"Employee deactivated",
					"%s was deactivated successfully.".formatted(fullName(savedEmployee)),
					NotificationType.EMPLOYEE_DEACTIVATED);
		}

		return new EmployeeDeactivationResponse(
				true,
				"Employee deactivated successfully.",
				EmployeeResponse.from(savedEmployee));
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

	private void ensureCanDeactivate(AppUser actor, Employee employee, Optional<AppUser> linkedUser) {
		if (!hasPeopleAccess(actor)) {
			throw new EmployeeAccessDeniedException("You do not have permission to deactivate employees.");
		}

		if (isFounderEmployee(employee)) {
			throw new ProtectedEmployeeException("This employee is protected and cannot be deactivated.");
		}

		if (linkedUser.isEmpty()) {
			return;
		}

		AppUser targetUser = linkedUser.get();
		if (actor.getId() != null && actor.getId().equals(targetUser.getId())) {
			throw new ProtectedEmployeeException("You cannot deactivate your own employee profile.");
		}

		if (targetUser.getRole() == UserRole.FOUNDER || isPrimaryAdmin(targetUser)) {
			throw new ProtectedEmployeeException("This employee is protected and cannot be deactivated.");
		}

		if (actor.getRole() == UserRole.HR && (targetUser.getRole() == UserRole.ADMIN || targetUser.getRole() == UserRole.FOUNDER)) {
			throw new EmployeeAccessDeniedException("HR cannot deactivate ADMIN or FOUNDER employees.");
		}
	}

	private boolean isPrimaryAdmin(AppUser user) {
		if (user == null || user.getRole() != UserRole.ADMIN) {
			return false;
		}

		String userEmail = normalize(user.getEmail());
		return (!primaryAdminEmail.isBlank() && primaryAdminEmail.equals(userEmail))
				|| "system".equalsIgnoreCase(user.getCreatedBy());
	}

	private boolean isFounderEmployee(Employee employee) {
		String jobTitle = normalize(employee == null ? null : employee.getJobTitle());
		return jobTitle.contains("founder");
	}

	private String fullName(Employee employee) {
		if (employee == null) {
			return "Employee";
		}

		String name = ("%s %s".formatted(
				employee.getFirstName() == null ? "" : employee.getFirstName(),
				employee.getLastName() == null ? "" : employee.getLastName())).trim();
		return name.isBlank() ? "Employee" : name;
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}
}
