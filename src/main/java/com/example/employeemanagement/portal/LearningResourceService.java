package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LearningResourceService {

	private final LearningResourceRepository learningResourceRepository;

	public LearningResourceService(LearningResourceRepository learningResourceRepository) {
		this.learningResourceRepository = learningResourceRepository;
	}

	@Transactional(readOnly = true)
	public List<LearningResource> getResources() {
		return learningResourceRepository.findAllByOrderByCreatedAtDesc();
	}

	public LearningResource createResource(LearningResource request) {
		LearningResource resource = new LearningResource();
		apply(request, resource);
		return learningResourceRepository.save(resource);
	}

	public LearningResource updateResource(Long id, LearningResource request) {
		LearningResource resource = learningResourceRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Learning resource", id));
		apply(request, resource);
		return learningResourceRepository.save(resource);
	}

	public void deleteResource(Long id) {
		if (!learningResourceRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Learning resource", id);
		}

		learningResourceRepository.deleteById(id);
	}

	private void apply(LearningResource source, LearningResource target) {
		target.setTitle(defaulted(source.getTitle(), "Learning resource"));
		target.setType(defaulted(source.getType(), "COURSE").toUpperCase());
		target.setLink(defaulted(source.getLink(), "#"));
		target.setDescription(clean(source.getDescription()));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String defaulted(String value, String fallback) {
		String cleaned = clean(value);
		return cleaned == null ? fallback : cleaned;
	}
}
