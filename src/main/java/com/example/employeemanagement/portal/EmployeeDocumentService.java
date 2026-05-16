package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class EmployeeDocumentService {

	private final EmployeeDocumentRepository employeeDocumentRepository;

	public EmployeeDocumentService(EmployeeDocumentRepository employeeDocumentRepository) {
		this.employeeDocumentRepository = employeeDocumentRepository;
	}

	@Transactional(readOnly = true)
	public List<EmployeeDocument> getDocuments(Long employeeId) {
		if (employeeId != null) {
			return employeeDocumentRepository.findByEmployeeIdOrderByUploadedAtDesc(employeeId);
		}

		return employeeDocumentRepository.findAllByOrderByUploadedAtDesc();
	}

	public EmployeeDocument createDocument(EmployeeDocument request) {
		EmployeeDocument document = new EmployeeDocument();
		apply(request, document);
		return employeeDocumentRepository.save(document);
	}

	public EmployeeDocument updateDocument(Long id, EmployeeDocument request) {
		EmployeeDocument document = employeeDocumentRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Employee document", id));
		apply(request, document);
		return employeeDocumentRepository.save(document);
	}

	public void deleteDocument(Long id) {
		if (!employeeDocumentRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Employee document", id);
		}

		employeeDocumentRepository.deleteById(id);
	}

	private void apply(EmployeeDocument source, EmployeeDocument target) {
		target.setEmployeeId(source.getEmployeeId() == null ? 0L : source.getEmployeeId());
		target.setDocumentType(defaulted(source.getDocumentType(), "Document"));
		target.setDocumentUrl(defaulted(source.getDocumentUrl(), "#"));
		target.setUploadedAt(source.getUploadedAt() == null ? Instant.now() : source.getUploadedAt());
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String defaulted(String value, String fallback) {
		String cleaned = clean(value);
		return cleaned == null ? fallback : cleaned;
	}
}
