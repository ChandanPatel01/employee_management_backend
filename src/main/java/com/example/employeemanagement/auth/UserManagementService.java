package com.example.employeemanagement.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserManagementService {

	private final AppUserRepository appUserRepository;
	private final PasswordService passwordService;

	public UserManagementService(AppUserRepository appUserRepository, PasswordService passwordService) {
		this.appUserRepository = appUserRepository;
		this.passwordService = passwordService;
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

	public ManagedUserResponse createUser(CreateUserRequest request, long actorId) {
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

		return ManagedUserResponse.from(appUserRepository.save(user), actor.getRole() == UserRole.ADMIN);
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
}
