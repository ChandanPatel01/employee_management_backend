package com.example.employeemanagement.employee;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class EmployeeExceptionHandler {

	@ExceptionHandler(EmployeeNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleEmployeeNotFound(EmployeeNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateEmployeeEmailException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ApiError handleDuplicateEmployeeEmail(DuplicateEmployeeEmailException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> validationErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors().forEach(fieldError ->
				validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));

		return error(HttpStatus.BAD_REQUEST, "Validation failed", validationErrors);
	}

	private ApiError error(HttpStatus status, String message, Map<String, String> validationErrors) {
		return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, validationErrors);
	}
}
