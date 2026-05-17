package com.example.employeemanagement.upload;

import com.example.employeemanagement.workflow.NotificationService;
import com.example.employeemanagement.workflow.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

	private final EmployeePhotoStorageService employeePhotoStorageService;
	private final NotificationService notificationService;

	public UploadController(EmployeePhotoStorageService employeePhotoStorageService, NotificationService notificationService) {
		this.employeePhotoStorageService = employeePhotoStorageService;
		this.notificationService = notificationService;
	}

	@PostMapping(value = "/employee-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public UploadResponse uploadEmployeePhoto(
			@RequestParam("file") MultipartFile file,
			@RequestParam(name = "employeeId", required = false) Long employeeId) {
		String photoUrl = employeePhotoStorageService.store(file);
		notifyDocumentUploadedSafely(employeeId);
		return new UploadResponse(photoUrl);
	}

	public record UploadResponse(String photoUrl) {
	}

	private void notifyDocumentUploadedSafely(Long employeeId) {
		if (employeeId == null) {
			return;
		}

		try {
			notificationService.notifyEmployee(
					employeeId,
					"Document uploaded",
					"A document or profile image was uploaded to your employee profile.",
					NotificationType.DOCUMENT_UPLOADED);
		} catch (Exception exception) {
			logger.warn("Document upload notification could not be stored for employee {}.", employeeId, exception);
		}
	}
}
