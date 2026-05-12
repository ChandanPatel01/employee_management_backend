package com.example.employeemanagement.timeoff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

	List<LeaveRequest> findByEmployeeId(Long employeeId);

	List<LeaveRequest> findByStatus(LeaveStatus status);

	List<LeaveRequest> findByStatusAndEmployeeId(LeaveStatus status, Long employeeId);
}
