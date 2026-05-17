package com.example.employeemanagement.auth;

import com.example.employeemanagement.employee.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final ObjectMapper objectMapper;
	private final AppUserRepository appUserRepository;

	public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper, AppUserRepository appUserRepository) {
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
		this.appUserRepository = appUserRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (shouldSkip(request)) {
			filterChain.doFilter(request, response);
			return;
		}

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			unauthorized(response, "Missing bearer token");
			return;
		}

		try {
			JwtService.JwtClaims claims = jwtService.validate(authorization.substring(7));
			AppUser user = appUserRepository.findById(claims.userId()).orElse(null);
			if (user == null) {
				unauthorized(response, "Authenticated user was not found");
				return;
			}

			if (user.isForcePasswordChange() && !isChangePasswordRequest(request)) {
				forbidden(response, "Please change your temporary password before continuing.");
				return;
			}

			if (!isRoleAllowed(request, user.getRole())) {
				forbidden(response, "Access denied. You do not have permission to access this module.");
				return;
			}

			request.setAttribute("authenticatedUserEmail", user.getEmail());
			request.setAttribute("authenticatedUserId", user.getId());
			request.setAttribute("authenticatedUserRole", user.getRole().name());
			filterChain.doFilter(request, response);
		} catch (JwtService.JwtValidationException exception) {
			unauthorized(response, exception.getMessage());
		}
	}

	private boolean shouldSkip(HttpServletRequest request) {
		String path = request.getRequestURI();
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
				|| !path.startsWith("/api/")
				|| path.equals("/api/auth/login")
				|| path.equals("/api/auth/signup");
	}

	private boolean isChangePasswordRequest(HttpServletRequest request) {
		return "/api/auth/change-password".equals(request.getRequestURI());
	}

	private boolean isRoleAllowed(HttpServletRequest request, UserRole role) {
		String path = request.getRequestURI();

		if (isChangePasswordRequest(request)) {
			return true;
		}

		if (path.startsWith("/api/users")) {
			return hasAny(role, UserRole.ADMIN, UserRole.FOUNDER, UserRole.HR);
		}

		if (path.startsWith("/api/employees")
				|| path.startsWith("/api/leaves")
				|| path.startsWith("/api/uploads")) {
			return hasAny(role, UserRole.ADMIN, UserRole.FOUNDER, UserRole.HR);
		}

		if (path.startsWith("/api/crm/")) {
			return hasAny(role, UserRole.ADMIN, UserRole.FOUNDER, UserRole.MANAGER);
		}

		return role == UserRole.ADMIN || role == UserRole.FOUNDER;
	}

	private boolean hasAny(UserRole actualRole, UserRole... allowedRoles) {
		for (UserRole allowedRole : allowedRoles) {
			if (actualRole == allowedRole) {
				return true;
			}
		}

		return false;
	}

	private void unauthorized(HttpServletResponse response, String message) throws IOException {
		ApiError error = new ApiError(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				message,
				Map.of());

		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), error);
	}

	private void forbidden(HttpServletResponse response, String message) throws IOException {
		ApiError error = new ApiError(
				Instant.now(),
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				message,
				Map.of());

		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), error);
	}
}
