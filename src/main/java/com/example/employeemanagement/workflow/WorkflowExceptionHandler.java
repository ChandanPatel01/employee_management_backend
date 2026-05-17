package com.example.employeemanagement.workflow;

import com.example.employeemanagement.employee.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class WorkflowExceptionHandler {

	@ExceptionHandler(WorkflowException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public ApiError handleWorkflowException(WorkflowException exception) {
		return new ApiError(
				Instant.now(),
				HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(),
				exception.getMessage(),
				Map.of());
	}
}
