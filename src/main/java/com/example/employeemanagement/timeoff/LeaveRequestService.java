package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LeaveRequestService {

	private final LeaveRequestRepository leaveRequestRepository;
	private final EmployeeService employeeService;

	public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, EmployeeService employeeService) {
		this.leaveRequestRepository = leaveRequestRepository;
		this.employeeService = employeeService;
	}

	@Transactional(readOnly = true)
	public List<LeaveRequest> getLeaves(LeaveStatus status, Long employeeId) {
		if (status != null && employeeId != null) {
			return leaveRequestRepository.findByStatusAndEmployeeId(status, employeeId);
		}

		if (status != null) {
			return leaveRequestRepository.findByStatus(status);
		}

		if (employeeId != null) {
			return leaveRequestRepository.findByEmployeeId(employeeId);
		}

		return leaveRequestRepository.findAll();
	}

	public LeaveRequest createLeave(LeaveCreateRequest request) {
		Employee employee = employeeService.getEmployee(request.employeeId());

		LeaveRequest leaveRequest = new LeaveRequest();
		leaveRequest.setEmployee(employee);
		leaveRequest.setLeaveType(request.leaveType().trim());
		leaveRequest.setFromDate(request.fromDate());
		leaveRequest.setToDate(request.toDate());
		leaveRequest.setDescription(clean(request.description()));
		leaveRequest.setAppliedDate(LocalDate.now());
		leaveRequest.setStatus(LeaveStatus.PENDING);

		return leaveRequestRepository.save(leaveRequest);
	}

	public LeaveRequest updateDecision(Long id, LeaveDecisionRequest request) {
		LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
				.orElseThrow(() -> new LeaveNotFoundException(id));

		leaveRequest.setStatus(request.status());
		leaveRequest.setDecisionReason(clean(request.reason()));
		leaveRequest.setDecidedAt(Instant.now());

		return leaveRequestRepository.save(leaveRequest);
	}

	public void deleteLeave(Long id) {
		if (!leaveRequestRepository.existsById(id)) {
			throw new LeaveNotFoundException(id);
		}

		leaveRequestRepository.deleteById(id);
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
