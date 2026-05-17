package com.example.employeemanagement.timeoff;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

	@Override
	@EntityGraph(attributePaths = "employee")
	List<LeaveRequest> findAll();

	@Override
	@EntityGraph(attributePaths = "employee")
	Optional<LeaveRequest> findById(Long id);

	@EntityGraph(attributePaths = "employee")
	List<LeaveRequest> findByEmployeeId(Long employeeId);

	@EntityGraph(attributePaths = "employee")
	List<LeaveRequest> findByStatus(LeaveStatus status);

	@EntityGraph(attributePaths = "employee")
	List<LeaveRequest> findByStatusAndEmployeeId(LeaveStatus status, Long employeeId);
}
