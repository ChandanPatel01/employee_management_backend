package com.example.employeemanagement.auth;

import com.example.employeemanagement.employee.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

	@ExceptionHandler(DuplicateUserEmailException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleDuplicateUserEmail(DuplicateUserEmailException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiError handleInvalidCredentials(InvalidCredentialsException exception) {
		return error(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(SignupDisabledException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiError handleSignupDisabled(SignupDisabledException exception) {
		return error(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(AccountBlockedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiError handleAccountBlocked(AccountBlockedException exception) {
		return error(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(PasswordChangeException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handlePasswordChange(PasswordChangeException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(UserManagementException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiError handleUserManagement(UserManagementException exception) {
		return error(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	private ApiError error(HttpStatus status, String message) {
		return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, Map.of());
	}
}
