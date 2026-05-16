package com.example.employeemanagement.portal;

import com.example.employeemanagement.employee.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = {
		PortalController.class,
		TeamLeadController.class,
		ProjectController.class,
		ReportController.class,
		HrController.class,
		RoleManagementController.class
})
public class PortalExceptionHandler {

	@ExceptionHandler(PortalResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ApiError handleNotFound(PortalResourceNotFoundException exception) {
		return new ApiError(
				Instant.now(),
				HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(),
				exception.getMessage(),
				Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> validationErrors = new HashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

		return new ApiError(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				"Please fix the highlighted fields.",
				validationErrors);
	}
}
