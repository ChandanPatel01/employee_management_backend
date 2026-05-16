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

	public JwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper) {
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
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
			if (claims.role() != UserRole.ADMIN) {
				forbidden(response, "Access denied. Only admin users can access this management portal.");
				return;
			}

			request.setAttribute("authenticatedUserEmail", claims.email());
			request.setAttribute("authenticatedUserId", claims.userId());
			request.setAttribute("authenticatedUserRole", claims.role().name());
			filterChain.doFilter(request, response);
		} catch (JwtService.JwtValidationException exception) {
			unauthorized(response, exception.getMessage());
		}
	}

	private boolean shouldSkip(HttpServletRequest request) {
		String path = request.getRequestURI();
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
				|| !path.startsWith("/api/")
				|| path.startsWith("/api/auth/");
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
