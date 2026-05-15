package com.example.employeemanagement.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeePhotoStorageService {

	private static final Pattern DATA_URL_PATTERN = Pattern.compile("^data:([^;]+);base64,(.+)$");
	private static final String CLOUDINARY_FOLDER = "employee-management";

	private final Cloudinary cloudinary;

	public EmployeePhotoStorageService(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new UploadException("Please select an image.");
		}

		validateImageContentType(file.getContentType());

		try {
			Map<?, ?> uploadResult = cloudinary.uploader().upload(
					file.getBytes(),
					uploadOptions(uniquePublicId("employee")));
			return secureUrl(uploadResult);
		} catch (UploadException exception) {
			throw exception;
		} catch (IOException exception) {
			throw new UploadException("Could not upload employee image.");
		} catch (RuntimeException exception) {
			throw new UploadException("Cloudinary image upload failed.");
		}
	}

	public String storeDataUrl(Long employeeId, String dataUrl) {
		Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl == null ? "" : dataUrl);
		if (!matcher.matches()) {
			throw new UploadException("Invalid employee image data.");
		}

		validateImageContentType(matcher.group(1));

		byte[] imageBytes;
		try {
			imageBytes = Base64.getDecoder().decode(matcher.group(2));
		} catch (IllegalArgumentException exception) {
			throw new UploadException("Invalid employee image data.");
		}

		try {
			Map<?, ?> uploadResult = cloudinary.uploader().upload(
					imageBytes,
					uploadOptions(uniquePublicId("employee-" + employeeId + "-migrated")));
			return secureUrl(uploadResult);
		} catch (UploadException exception) {
			throw exception;
		} catch (IOException exception) {
			throw new UploadException("Could not migrate employee image.");
		} catch (RuntimeException exception) {
			throw new UploadException("Cloudinary image migration failed.");
		}
	}

	private Map<?, ?> uploadOptions(String publicId) {
		return ObjectUtils.asMap(
				"folder", CLOUDINARY_FOLDER,
				"public_id", publicId,
				"resource_type", "image",
				"overwrite", false);
	}

	private String uniquePublicId(String prefix) {
		return prefix + "-" + UUID.randomUUID();
	}

	private String secureUrl(Map<?, ?> uploadResult) {
		Object secureUrl = uploadResult.get("secure_url");
		if (secureUrl instanceof String value && !value.isBlank()) {
			return value;
		}

		throw new UploadException("Cloudinary did not return an image URL.");
	}

	private void validateImageContentType(String contentType) {
		switch ((contentType == null ? "" : contentType).toLowerCase(Locale.ROOT)) {
			case "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif" -> {
			}
			default -> throw new UploadException("Only JPG, PNG, WEBP, or GIF images are allowed.");
		}
	}
}
