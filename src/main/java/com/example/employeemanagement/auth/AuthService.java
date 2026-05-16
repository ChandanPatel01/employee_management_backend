package com.example.employeemanagement.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		user.setRole(UserRole.EMPLOYEE);

		AppUser savedUser = appUserRepository.save(user);
		return responseFor(savedUser);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(AuthRequest request) {
		String email = normalizeEmail(request.email());
		AppUser user = appUserRepository.findByEmail(email)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordService.verify(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return responseFor(user);
	}

	private AuthResponse responseFor(AppUser user) {
		String token = jwtService.createToken(user);
		return new AuthResponse(token, "Bearer", jwtService.getExpiresInSeconds(), UserResponse.from(user));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
