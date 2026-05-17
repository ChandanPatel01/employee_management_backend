package com.example.employeemanagement.auth;

import com.example.employeemanagement.email.BrevoEmailService;
import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmployeeRepository;
import com.example.employeemanagement.employee.EmployeeService;
import com.example.employeemanagement.employee.EmploymentStatus;
import com.example.employeemanagement.workflow.NotificationService;
import com.example.employeemanagement.workflow.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class UserManagementService {

	private static final Logger logger = LoggerFactory.getLogger(UserManagementService.class);
	private static final String ONBOARDING_EMAIL_SENT_MESSAGE = "User created and onboarding email sent.";
	private static final String ONBOARDING_EMAIL_FAILED_MESSAGE = "User created, but onboarding email could not be sent.";

	private final AppUserRepository appUserRepository;
	private final PasswordService passwordService;
	private final BrevoEmailService brevoEmailService;
	private final EmployeeService employeeService;
	private final EmployeeRepository employeeRepository;
	private final NotificationService notificationService;

	public UserManagementService(
			AppUserRepository appUserRepository,
			PasswordService passwordService,
			BrevoEmailService brevoEmailService,
			EmployeeService employeeService,
			EmployeeRepository employeeRepository,
			NotificationService notificationService) {
		this.appUserRepository = appUserRepository;
		this.passwordService = passwordService;
		this.brevoEmailService = brevoEmailService;
		this.employeeService = employeeService;
		this.employeeRepository = employeeRepository;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<ManagedUserResponse> getUsers(long actorId) {
		AppUser actor = getActor(actorId);
		ensureCanCreateUsers(actor.getRole());
		boolean includePasswordTracking = canViewPasswordTracking(actor.getRole());

		return appUserRepository.findAll().stream()
				.map((user) -> ManagedUserResponse.from(user, includePasswordTracking))
				.toList();
	}

	public CreateUserResponse createUser(CreateUserRequest request, long actorId) {
		AppUser actor = getActor(actorId);
		UserRole newRole = request.role();
		String temporaryPassword = requireTemporaryPassword(request.temporaryPassword());

		ensureCanCreateUsers(actor.getRole());
		ensureCanCreateRole(actor.getRole(), newRole);

		String loginEmail = loginEmailFor(request);
		ensureEmailHasNoLogin(loginEmail);

		Employee employee = resolveEmployee(request, newRole);
		ensureEmployeeHasNoLogin(employee);

		AppUser user = new AppUser();
		user.setName(fullName(employee));
		user.setEmail(normalizeEmail(employee.getEmail()));
		user.setEmployee(employee);
		user.setRole(newRole);
		user.setPasswordHash(passwordService.hash(temporaryPassword));
		user.setForcePasswordChange(true);
		user.setPasswordChanged(false);
		user.setPasswordChangedAt(null);
		user.setCreatedBy(actor.getEmail());

		AppUser savedUser = appUserRepository.saveAndFlush(user);
		notifyOnboardingCompleteSafely(savedUser);
		boolean onboardingEmailSent = sendOnboardingEmailSafely(savedUser, temporaryPassword);
		String message = onboardingEmailSent ? ONBOARDING_EMAIL_SENT_MESSAGE : ONBOARDING_EMAIL_FAILED_MESSAGE;

		return CreateUserResponse.from(savedUser, canViewPasswordTracking(actor.getRole()), onboardingEmailSent, message);
	}

	public ManagedUserResponse setUserBlocked(Long userId, boolean blocked, long actorId) {
		AppUser actor = getActor(actorId);
		ensureAdminOrFounder(actor.getRole());
		AppUser user = getTargetUser(userId);
		ensureNotSelf(actor, user);

		user.setBlocked(blocked);
		return ManagedUserResponse.from(appUserRepository.save(user), true);
	}

	public ManagedUserResponse resetUserPassword(Long userId, String temporaryPassword, long actorId) {
		AppUser actor = getActor(actorId);
		ensureAdminOrFounder(actor.getRole());
		AppUser user = getTargetUser(userId);
		ensureNotSelf(actor, user);

		user.setPasswordHash(passwordService.hash(requireTemporaryPassword(temporaryPassword)));
		user.setForcePasswordChange(true);
		user.setPasswordChanged(false);
		user.setPasswordChangedAt(null);
		return ManagedUserResponse.from(appUserRepository.save(user), true);
	}

	private AppUser getActor(long actorId) {
		return appUserRepository.findById(actorId)
				.orElseThrow(() -> new UserManagementException("Authenticated user was not found"));
	}

	private AppUser getTargetUser(Long userId) {
		if (userId == null) {
			throw new UserManagementException("User is required.");
		}

		return appUserRepository.findById(userId)
				.orElseThrow(() -> new UserManagementException("User was not found."));
	}

	private void ensureCanCreateUsers(UserRole actorRole) {
		if (actorRole != UserRole.ADMIN && actorRole != UserRole.FOUNDER && actorRole != UserRole.HR) {
			throw new UserManagementException("Only ADMIN, FOUNDER, or HR can create users.");
		}
	}

	private void ensureAdminOrFounder(UserRole actorRole) {
		if (actorRole == UserRole.ADMIN || actorRole == UserRole.FOUNDER) {
			return;
		}

		throw new UserManagementException("Only ADMIN or FOUNDER can manage account access.");
	}

	private void ensureNotSelf(AppUser actor, AppUser user) {
		if (actor.getId() != null && actor.getId().equals(user.getId())) {
			throw new UserManagementException("Use Change Password for your own account.");
		}
	}

	private void ensureCanCreateRole(UserRole actorRole, UserRole newRole) {
		if (actorRole == UserRole.ADMIN || actorRole == UserRole.FOUNDER) {
			return;
		}

		if (newRole == UserRole.EMPLOYEE || newRole == UserRole.INTERN || newRole == UserRole.MANAGER) {
			return;
		}

		throw new UserManagementException("HR can create only EMPLOYEE, INTERN, or MANAGER users.");
	}

	private boolean canViewPasswordTracking(UserRole actorRole) {
		return actorRole == UserRole.ADMIN || actorRole == UserRole.FOUNDER;
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String requireTemporaryPassword(String temporaryPassword) {
		if (temporaryPassword == null || temporaryPassword.isBlank()) {
			throw new UserManagementException("Temporary password is required.");
		}

		if (temporaryPassword.length() < 8) {
			throw new UserManagementException("Temporary password must be at least 8 characters");
		}

		return temporaryPassword;
	}

	private String loginEmailFor(CreateUserRequest request) {
		if (request.employeeId() != null) {
			Employee employee = employeeService.getEmployee(request.employeeId());
			return normalizeEmail(requireValue(employee.getEmail(), "Selected employee email is missing."));
		}

		if (request.employee() != null) {
			return normalizeEmail(requireValue(request.employee().email(), "Employee email is required."));
		}

		return normalizeEmail(requireValue(request.email(), "Employee email is required."));
	}

	private void ensureEmailHasNoLogin(String email) {
		if (appUserRepository.existsByEmail(email)) {
			throw new DuplicateUserEmailException(email);
		}
	}

	private void ensureEmployeeHasNoLogin(Employee employee) {
		if (employee.getId() != null && appUserRepository.existsByEmployeeId(employee.getId())) {
			throw new UserManagementException("This employee already has a login account.");
		}
	}

	private Employee resolveEmployee(CreateUserRequest request, UserRole role) {
		if (request.employeeId() != null) {
			return employeeService.getEmployee(request.employeeId());
		}

		if (request.employee() != null) {
			return createEmployeeFromRequest(request.employee(), role);
		}

		String email = normalizeEmail(requireValue(request.email(), "Employee email is required."));
		return employeeRepository.findByEmailIgnoreCase(email)
				.orElseGet(() -> createMinimalEmployeeProfile(request.name(), email, role));
	}

	private Employee createEmployeeFromRequest(CreateUserRequest.EmployeeProfileRequest request, UserRole role) {
		Employee employee = new Employee();
		employee.setFirstName(requireValue(request.firstName(), "Employee first name is required."));
		employee.setLastName(requireValue(request.lastName(), "Employee last name is required."));
		employee.setEmail(normalizeEmail(requireValue(request.email(), "Employee email is required.")));
		employee.setDepartment(requireValue(request.department(), "Employee department is required."));
		employee.setJobTitle(requireValue(request.jobTitle(), "Employee designation is required."));
		employee.setPhone(clean(request.phone()));
		employee.setSalary(request.salary() == null ? BigDecimal.ZERO : request.salary());
		employee.setHireDate(request.hireDate() == null ? LocalDate.now() : request.hireDate());
		employee.setStatus(request.status() == null ? EmploymentStatus.ACTIVE : request.status());
		employee.setPhotoUrl(clean(request.photoUrl()));

		return employeeService.createEmployee(employee);
	}

	private Employee createMinimalEmployeeProfile(String name, String email, UserRole role) {
		String cleanName = clean(name);
		String firstName = firstNameFrom(cleanName, role);
		String lastName = lastNameFrom(cleanName);

		Employee employee = new Employee();
		employee.setFirstName(firstName);
		employee.setLastName(lastName);
		employee.setEmail(email);
		employee.setDepartment(defaultDepartment(role));
		employee.setJobTitle(defaultDesignation(role));
		employee.setSalary(BigDecimal.ZERO);
		employee.setHireDate(LocalDate.now());
		employee.setStatus(EmploymentStatus.ACTIVE);

		return employeeService.createEmployee(employee);
	}

	private String requireValue(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new UserManagementException(message);
		}

		return value.trim();
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String fullName(Employee employee) {
		return (employee.getFirstName() + " " + employee.getLastName()).trim();
	}

	private String firstNameFrom(String name, UserRole role) {
		if (name == null) {
			return defaultDesignation(role);
		}

		return name.split("\\s+", 2)[0];
	}

	private String lastNameFrom(String name) {
		if (name == null || !name.trim().contains(" ")) {
			return "User";
		}

		String[] parts = name.trim().split("\\s+", 2);
		return parts[1].isBlank() ? "User" : parts[1];
	}

	private String defaultDepartment(UserRole role) {
		return switch (role) {
			case FOUNDER -> "Leadership";
			case ADMIN -> "Administration";
			case HR -> "Human Resources";
			case MANAGER -> "Management";
			case INTERN -> "Internship";
			case EMPLOYEE -> "Operations";
		};
	}

	private String defaultDesignation(UserRole role) {
		return switch (role) {
			case FOUNDER -> "Founder";
			case ADMIN -> "Administrator";
			case HR -> "HR Executive";
			case MANAGER -> "Manager";
			case INTERN -> "Intern";
			case EMPLOYEE -> "Employee";
		};
	}

	private boolean sendOnboardingEmailSafely(AppUser user, String temporaryPassword) {
		try {
			return brevoEmailService.sendOnboardingEmail(
					user.getName(),
					user.getEmail(),
					temporaryPassword,
					user.getRole());
		} catch (Exception exception) {
			logger.error("User {} was created, but onboarding email failed.", user.getId(), exception);
			return false;
		}
	}

	private void notifyOnboardingCompleteSafely(AppUser user) {
		try {
			notificationService.notifyUser(
					user,
					"Welcome to MensPingo EMS",
					"Your MensPingo Employee Management System account is ready.",
					NotificationType.ONBOARDING_COMPLETE);
		} catch (Exception exception) {
			logger.warn("User {} was created, but onboarding notification could not be stored.", user.getId(), exception);
		}
	}
}
