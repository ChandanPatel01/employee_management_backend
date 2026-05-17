package com.example.employeemanagement.auth;

import com.example.employeemanagement.email.BrevoEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public UserManagementService(
			AppUserRepository appUserRepository,
			PasswordService passwordService,
			BrevoEmailService brevoEmailService) {
		this.appUserRepository = appUserRepository;
		this.passwordService = passwordService;
		this.brevoEmailService = brevoEmailService;
	}

	@Transactional(readOnly = true)
	public List<ManagedUserResponse> getUsers(long actorId) {
		AppUser actor = getActor(actorId);
		ensureCanCreateUsers(actor.getRole());
		boolean includePasswordTracking = actor.getRole() == UserRole.ADMIN;

		return appUserRepository.findAll().stream()
				.map((user) -> ManagedUserResponse.from(user, includePasswordTracking))
				.toList();
	}

	public CreateUserResponse createUser(CreateUserRequest request, long actorId) {
		AppUser actor = getActor(actorId);
		UserRole newRole = request.role();

		ensureCanCreateUsers(actor.getRole());
		ensureCanCreateRole(actor.getRole(), newRole);

		String email = normalizeEmail(request.email());
		if (appUserRepository.existsByEmail(email)) {
			throw new DuplicateUserEmailException(email);
		}

		AppUser user = new AppUser();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setRole(newRole);
		user.setPasswordHash(passwordService.hash(request.temporaryPassword()));
		user.setForcePasswordChange(true);
		user.setPasswordChanged(false);
		user.setPasswordChangedAt(null);
		user.setCreatedBy(actor.getEmail());

		AppUser savedUser = appUserRepository.saveAndFlush(user);
		boolean onboardingEmailSent = sendOnboardingEmailSafely(savedUser, request.temporaryPassword());
		String message = onboardingEmailSent ? ONBOARDING_EMAIL_SENT_MESSAGE : ONBOARDING_EMAIL_FAILED_MESSAGE;

		return CreateUserResponse.from(savedUser, actor.getRole() == UserRole.ADMIN, onboardingEmailSent, message);
	}

	private AppUser getActor(long actorId) {
		return appUserRepository.findById(actorId)
				.orElseThrow(() -> new UserManagementException("Authenticated user was not found"));
	}

	private void ensureCanCreateUsers(UserRole actorRole) {
		if (actorRole != UserRole.ADMIN && actorRole != UserRole.HR) {
			throw new UserManagementException("Only ADMIN or HR can create users.");
		}
	}

	private void ensureCanCreateRole(UserRole actorRole, UserRole newRole) {
		if (actorRole == UserRole.ADMIN) {
			return;
		}

		if (newRole == UserRole.EMPLOYEE || newRole == UserRole.INTERN || newRole == UserRole.MANAGER) {
			return;
		}

		throw new UserManagementException("HR can create only EMPLOYEE, INTERN, or MANAGER users.");
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
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
}
