package com.example.employeemanagement.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

	List<Attendance> findAllByOrderByAttendanceDateDesc();

	List<Attendance> findByUserEmailIgnoreCaseOrderByAttendanceDateDesc(String userEmail);
}
