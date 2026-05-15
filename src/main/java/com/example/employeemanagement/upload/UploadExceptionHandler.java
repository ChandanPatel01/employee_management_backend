package com.example.employeemanagement.upload;

import com.example.employeemanagement.employee.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class UploadExceptionHandler {

	@ExceptionHandler(UploadException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleUploadException(UploadException exception) {
		return new ApiError(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(),
				exception.getMessage(),
				Map.of());
	}
}
