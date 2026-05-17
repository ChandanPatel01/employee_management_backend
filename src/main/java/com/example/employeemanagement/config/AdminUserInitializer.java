package com.example.employeemanagement.config;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.PasswordService;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmployeeRepository;
import com.example.employeemanagement.employee.EmployeeService;
import com.example.employeemanagement.employee.EmploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AdminUserInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

	private final AppUserRepository appUserRepository;
	private final EmployeeRepository employeeRepository;
	private final EmployeeService employeeService;
	private final PasswordService passwordService;
	private final String adminEmail;
	private final String adminPassword;
	private final String adminName;

	public AdminUserInitializer(
			AppUserRepository appUserRepository,
			EmployeeRepository employeeRepository,
			EmployeeService employeeService,
			PasswordService passwordService,
			@Value("${admin.email:}") String adminEmail,
			@Value("${admin.password:}") String adminPassword,
			@Value("${admin.name:}") String adminName) {
		this.appUserRepository = appUserRepository;
		this.employeeRepository = employeeRepository;
		this.employeeService = employeeService;
		this.passwordService = passwordService;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
		this.adminName = adminName;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (isBlank(adminEmail) || isBlank(adminPassword) || isBlank(adminName)) {
			logger.info("Initial admin user creation skipped because ADMIN_EMAIL, ADMIN_PASSWORD, or ADMIN_NAME is missing.");
			return;
		}

		String email = adminEmail.trim().toLowerCase();
		AppUser user = appUserRepository.findByEmail(email).orElseGet(AppUser::new);
		boolean isNewUser = user.getId() == null;

		if (isNewUser) {
			user.setEmail(email);
			user.setName(adminName.trim());
			user.setPasswordHash(passwordService.hash(adminPassword));
		}

		user.setRole(UserRole.ADMIN);
		user.setEmployee(employeeProfileFor(email, adminName.trim()));
		user.setForcePasswordChange(false);
		user.setPasswordChanged(true);
		if (user.getPasswordChangedAt() == null) {
			user.setPasswordChangedAt(Instant.now());
		}
		user.setCreatedBy("system");
		appUserRepository.save(user);

		logger.info("{} admin user for {}", isNewUser ? "Created" : "Confirmed", email);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private Employee employeeProfileFor(String email, String name) {
		return employeeRepository.findByEmailIgnoreCase(email)
				.orElseGet(() -> {
					Employee employee = new Employee();
					employee.setFirstName(firstName(name));
					employee.setLastName(lastName(name));
					employee.setEmail(email);
					employee.setDepartment("Administration");
					employee.setJobTitle("Administrator");
					employee.setSalary(BigDecimal.ZERO);
					employee.setHireDate(LocalDate.now());
					employee.setStatus(EmploymentStatus.ACTIVE);
					return employeeService.createEmployee(employee);
				});
	}

	private String firstName(String name) {
		if (isBlank(name)) {
			return "Admin";
		}

		return name.trim().split("\\s+", 2)[0];
	}

	private String lastName(String name) {
		if (isBlank(name) || !name.trim().contains(" ")) {
			return "User";
		}

		String[] parts = name.trim().split("\\s+", 2);
		return parts[1].isBlank() ? "User" : parts[1];
	}
}
