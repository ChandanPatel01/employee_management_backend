package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;

	public AttendanceService(AttendanceRepository attendanceRepository) {
		this.attendanceRepository = attendanceRepository;
	}

	@Transactional(readOnly = true)
	public List<Attendance> getAllAttendance() {
		return attendanceRepository.findAllByOrderByAttendanceDateDesc();
	}

	@Transactional(readOnly = true)
	public List<Attendance> getAttendanceForUser(String email) {
		return attendanceRepository.findByUserEmailIgnoreCaseOrderByAttendanceDateDesc(email);
	}

	public Attendance createAttendance(Attendance request) {
		Attendance attendance = new Attendance();
		apply(request, attendance);
		return attendanceRepository.save(attendance);
	}

	public Attendance updateAttendance(Long id, Attendance request) {
		Attendance attendance = attendanceRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Attendance", id));
		apply(request, attendance);
		return attendanceRepository.save(attendance);
	}

	public void deleteAttendance(Long id) {
		if (!attendanceRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Attendance", id);
		}

		attendanceRepository.deleteById(id);
	}

	private void apply(Attendance source, Attendance target) {
		target.setUserEmail(defaulted(source.getUserEmail(), "unassigned@menspingo.local"));
		target.setAttendanceDate(source.getAttendanceDate() == null ? LocalDate.now() : source.getAttendanceDate());
		target.setStatus(defaulted(source.getStatus(), "PRESENT").toUpperCase());
		target.setCheckInTime(source.getCheckInTime());
		target.setCheckOutTime(source.getCheckOutTime());
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
