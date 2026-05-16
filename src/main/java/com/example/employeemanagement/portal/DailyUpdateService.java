package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DailyUpdateService {

	private final DailyUpdateRepository dailyUpdateRepository;

	public DailyUpdateService(DailyUpdateRepository dailyUpdateRepository) {
		this.dailyUpdateRepository = dailyUpdateRepository;
	}

	@Transactional(readOnly = true)
	public List<DailyUpdate> getAllUpdates() {
		return dailyUpdateRepository.findAllByOrderByUpdateDateDesc();
	}

	@Transactional(readOnly = true)
	public List<DailyUpdate> getUpdatesForUser(String email) {
		return dailyUpdateRepository.findByUserEmailIgnoreCaseOrderByUpdateDateDesc(email);
	}

	public DailyUpdate createUpdate(String userEmail, DailyUpdate request) {
		DailyUpdate update = new DailyUpdate();
		apply(request, update, userEmail);
		return dailyUpdateRepository.save(update);
	}

	public DailyUpdate updateUpdate(Long id, String userEmail, DailyUpdate request) {
		DailyUpdate update = dailyUpdateRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Daily update", id));
		apply(request, update, userEmail);
		return dailyUpdateRepository.save(update);
	}

	public void deleteUpdate(Long id) {
		if (!dailyUpdateRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Daily update", id);
		}

		dailyUpdateRepository.deleteById(id);
	}

	private void apply(DailyUpdate source, DailyUpdate target, String userEmail) {
		target.setUserEmail(userEmail);
		target.setUpdateDate(source.getUpdateDate() == null ? LocalDate.now() : source.getUpdateDate());
		target.setWorkSummary(defaulted(source.getWorkSummary(), "Work update submitted."));
		target.setBlockers(clean(source.getBlockers()));
		target.setHoursWorked(source.getHoursWorked() == null ? BigDecimal.ZERO : source.getHoursWorked());
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String defaulted(String value, String fallback) {
		String cleaned = clean(value);
		return cleaned == null ? fallback : cleaned;
	}
}
