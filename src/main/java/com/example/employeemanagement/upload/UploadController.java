package com.example.employeemanagement.upload;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

	private final EmployeePhotoStorageService employeePhotoStorageService;

	public UploadController(EmployeePhotoStorageService employeePhotoStorageService) {
		this.employeePhotoStorageService = employeePhotoStorageService;
	}

	@PostMapping(value = "/employee-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public UploadResponse uploadEmployeePhoto(@RequestParam("file") MultipartFile file) {
		return new UploadResponse(employeePhotoStorageService.store(file));
	}

	public record UploadResponse(String photoUrl) {
	}
}
