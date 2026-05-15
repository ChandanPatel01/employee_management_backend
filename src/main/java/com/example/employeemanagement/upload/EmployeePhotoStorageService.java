package com.example.employeemanagement.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeePhotoStorageService {

	private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:([^;]+);base64,(.+)$");

	private final Path uploadRoot;
	private final Path employeePhotoDirectory;

	public EmployeePhotoStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
		this.uploadRoot = resolveUploadRoot(uploadDir);
		this.employeePhotoDirectory = this.uploadRoot.resolve("employee-photos");
	}

	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new UploadException("Please select an image.");
		}

		String extension = extensionFor(file.getContentType());
		String fileName = "employee-" + UUID.randomUUID() + extension;
		Path target = employeePhotoDirectory.resolve(fileName).normalize();

		try {
			Files.createDirectories(employeePhotoDirectory);
			file.transferTo(target);
			return "/uploads/employee-photos/" + fileName;
		} catch (IOException exception) {
			throw new UploadException("Could not save employee image.");
		}
	}

	public String storeDataUrl(Long employeeId, String dataUrl) {
		Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl == null ? "" : dataUrl);
		if (!matcher.matches()) {
			throw new UploadException("Invalid employee image data.");
		}

		String extension = extensionFor(matcher.group(1));
		byte[] imageBytes;
		try {
			imageBytes = Base64.getDecoder().decode(matcher.group(2));
		} catch (IllegalArgumentException exception) {
			throw new UploadException("Invalid employee image data.");
		}

		String fileName = "employee-" + employeeId + "-migrated-" + UUID.randomUUID() + extension;
		Path target = employeePhotoDirectory.resolve(fileName).normalize();

		try {
			Files.createDirectories(employeePhotoDirectory);
			Files.write(target, imageBytes);
			return "/uploads/employee-photos/" + fileName;
		} catch (IOException exception) {
			throw new UploadException("Could not migrate employee image.");
		}
	}

	public String uploadRootLocation() {
		return uploadRoot.toUri().toString();
	}

	private Path resolveUploadRoot(String uploadDir) {
		Path configuredPath = Paths.get(uploadDir);
		if (configuredPath.isAbsolute()) {
			return configuredPath.normalize();
		}

		Path backendProject = Paths.get("EmployeeManagement", "pom.xml");
		if (Files.exists(backendProject)) {
			return Paths.get("EmployeeManagement").resolve(configuredPath).toAbsolutePath().normalize();
		}

		return configuredPath.toAbsolutePath().normalize();
	}

	private String extensionFor(String contentType) {
		return switch ((contentType == null ? "" : contentType).toLowerCase(Locale.ROOT)) {
			case "image/jpeg", "image/jpg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/webp" -> ".webp";
			case "image/gif" -> ".gif";
			default -> throw new UploadException("Only JPG, PNG, WEBP, or GIF images are allowed.");
		};
	}
}
