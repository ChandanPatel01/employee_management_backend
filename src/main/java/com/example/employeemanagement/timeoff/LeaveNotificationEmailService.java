package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.email.BrevoEmailService;
import com.example.employeemanagement.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LeaveNotificationEmailService {

	private static final Logger logger = LoggerFactory.getLogger(LeaveNotificationEmailService.class);
	private static final String COMPANY_NAME = "MensPingo Tech Solutions";

	private final BrevoEmailService brevoEmailService;

	public LeaveNotificationEmailService(BrevoEmailService brevoEmailService) {
		this.brevoEmailService = brevoEmailService;
	}

	public boolean sendDecisionEmail(LeaveRequest leaveRequest) {
		Employee employee = leaveRequest.getEmployee();
		if (employee.getEmail() == null || employee.getEmail().isBlank()) {
			logger.warn("Could not send leave notification for leave {} because employee email is missing.", leaveRequest.getId());
			return false;
		}

		String employeeName = employee.getFirstName() + " " + employee.getLastName();
		return brevoEmailService.sendEmail(
				employee.getEmail(),
				employeeName,
				subjectFor(leaveRequest.getStatus()),
				bodyFor(leaveRequest, employee),
				"leave notification for leave " + leaveRequest.getId());
	}

	private String subjectFor(LeaveStatus status) {
		return switch (status) {
			case APPROVED -> "Leave Request Approved";
			case REJECTED -> "Leave Request Rejected";
			case PENDING -> "Leave Request Pending";
			case CANCELLED -> "Leave Request Cancelled";
		};
	}

	private String bodyFor(LeaveRequest leaveRequest, Employee employee) {
		String employeeName = employee.getFirstName() + " " + employee.getLastName();
		String reason = leaveRequest.getDecisionReason() == null || leaveRequest.getDecisionReason().isBlank()
				? "-"
				: leaveRequest.getDecisionReason();

		return """
				Hello %s,

				Your leave request status has been updated.

				Employee Name: %s
				Employee Email: %s
				Leave Type: %s
				From Date: %s
				To Date: %s
				Status: %s
				Reason/Comment: %s

				Company: %s
				""".formatted(
				employeeName,
				employeeName,
				employee.getEmail(),
				leaveRequest.getLeaveType(),
				leaveRequest.getFromDate(),
				leaveRequest.getToDate(),
				leaveRequest.getStatus(),
				reason,
				COMPANY_NAME);
	}
}
