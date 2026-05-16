package com.example.employeemanagement.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final JwtService jwtService;
	private final PasswordService passwordService;

	public AuthService(AppUserRepository appUserRepository, JwtService jwtService, PasswordService passwordService) {
		this.appUserRepository = appUserRepository;
		this.jwtService = jwtService;
		this.passwordService = passwordService;
	}

	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (appUserRepository.existsByEmail(email)) {
			throw new DuplicateUserEmailException(email);
		}

		AppUser user = new AppUser();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordService.hash(request.password()));

		AppUser savedUser = appUserRepository.save(user);
		return responseFor(savedUser);
	}

	public AuthResponse login(AuthRequest request) {
		String email = normalizeEmail(request.email());
		AppUser user = appUserRepository.findByEmail(email)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordService.verify(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		if (passwordService.needsRehash(user.getPasswordHash())) {
			user.setPasswordHash(passwordService.hash(request.password()));
			user = appUserRepository.save(user);
		}

		return responseFor(user);
	}

	public AuthResponse changePassword(long userId, ChangePasswordRequest request) {
		AppUser user = appUserRepository.findById(userId)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordService.verify(request.currentPassword(), user.getPasswordHash())) {
			throw new PasswordChangeException("Current password is incorrect");
		}

		if (!request.newPassword().equals(request.confirmPassword())) {
			throw new PasswordChangeException("New password and confirm password do not match");
		}

		if (passwordService.verify(request.newPassword(), user.getPasswordHash())) {
			throw new PasswordChangeException("New password must be different from the temporary password");
		}

		user.setPasswordHash(passwordService.hash(request.newPassword()));
		user.setForcePasswordChange(false);
		user.setPasswordChanged(true);
		user.setPasswordChangedAt(Instant.now());

		return responseFor(appUserRepository.save(user));
	}

	private AuthResponse responseFor(AppUser user) {
		String token = jwtService.createToken(user);
		UserResponse userResponse = UserResponse.from(user);
		return new AuthResponse(
				token,
				"Bearer",
				jwtService.getExpiresInSeconds(),
				userResponse,
				user.getRole(),
				user.getName(),
				user.isForcePasswordChange());
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
