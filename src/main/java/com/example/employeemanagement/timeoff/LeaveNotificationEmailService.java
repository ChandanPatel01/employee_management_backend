package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class LeaveNotificationEmailService {

	private static final Logger logger = LoggerFactory.getLogger(LeaveNotificationEmailService.class);
	private static final String COMPANY_NAME = "MensPingo Tech Solutions";

	private final JavaMailSender mailSender;
	private final String fromAddress;

	public LeaveNotificationEmailService(
			JavaMailSender mailSender,
			@Value("${mail.from}") String fromAddress) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
	}

	public boolean sendDecisionEmail(LeaveRequest leaveRequest) {
		Employee employee = leaveRequest.getEmployee();
		if (employee.getEmail() == null || employee.getEmail().isBlank()) {
			logger.warn("Could not send leave notification for leave {} because employee email is missing.", leaveRequest.getId());
			return false;
		}

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromAddress);
			message.setTo(employee.getEmail());
			message.setSubject(subjectFor(leaveRequest.getStatus()));
			message.setText(bodyFor(leaveRequest, employee));
			mailSender.send(message);
			return true;
		} catch (MailException exception) {
			logger.error("Could not send leave notification for leave {}.", leaveRequest.getId(), exception);
			return false;
		} catch (Exception exception) {
			logger.error("Unexpected leave notification error for leave {}.", leaveRequest.getId(), exception);
			return false;
		}
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
