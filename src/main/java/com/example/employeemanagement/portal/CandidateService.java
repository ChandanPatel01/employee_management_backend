package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CandidateService {

	private final CandidateRepository candidateRepository;

	public CandidateService(CandidateRepository candidateRepository) {
		this.candidateRepository = candidateRepository;
	}

	@Transactional(readOnly = true)
	public List<Candidate> getCandidates() {
		return candidateRepository.findAllByOrderByCreatedAtDesc();
	}

	public Candidate createCandidate(Candidate request) {
		Candidate candidate = new Candidate();
		apply(request, candidate);
		return candidateRepository.save(candidate);
	}

	public Candidate updateCandidate(Long id, Candidate request) {
		Candidate candidate = candidateRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Candidate", id));
		apply(request, candidate);
		return candidateRepository.save(candidate);
	}

	public void deleteCandidate(Long id) {
		if (!candidateRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Candidate", id);
		}

		candidateRepository.deleteById(id);
	}

	private void apply(Candidate source, Candidate target) {
		target.setName(defaulted(source.getName(), "Unnamed candidate"));
		target.setEmail(defaulted(source.getEmail(), "candidate@menspingo.local").toLowerCase());
		target.setPhone(clean(source.getPhone()));
		target.setSkill(clean(source.getSkill()));
		target.setExperience(clean(source.getExperience()));
		target.setResumeUrl(clean(source.getResumeUrl()));
		target.setStatus(defaulted(source.getStatus(), "NEW").toUpperCase());
		target.setInterviewDate(source.getInterviewDate());
		target.setNotes(clean(source.getNotes()));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String defaulted(String value, String fallback) {
		String cleaned = clean(value);
		return cleaned == null ? fallback : cleaned;
	}
}
