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

	private ApiError error(HttpStatus status, String message) {
		return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, Map.of());
	}
}
