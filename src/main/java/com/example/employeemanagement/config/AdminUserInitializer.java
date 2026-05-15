package com.example.employeemanagement.config;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.PasswordService;
import com.example.employeemanagement.auth.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AdminUserInitializer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

	private final AppUserRepository appUserRepository;
	private final PasswordService passwordService;
	private final String adminEmail;
	private final String adminPassword;
	private final String adminName;

	public AdminUserInitializer(
			AppUserRepository appUserRepository,
			PasswordService passwordService,
			@Value("${admin.email:}") String adminEmail,
			@Value("${admin.password:}") String adminPassword,
			@Value("${admin.name:}") String adminName) {
		this.appUserRepository = appUserRepository;
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
		appUserRepository.save(user);

		logger.info("{} admin user for {}", isNewUser ? "Created" : "Confirmed", email);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
