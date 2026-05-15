package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.employee.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class LeaveNotificationEmailService {

	private static final Logger logger = LoggerFactory.getLogger(LeaveNotificationEmailService.class);
	private static final String BREVO_EMAIL_ENDPOINT = "https://api.brevo.com/v3/smtp/email";
	private static final String COMPANY_NAME = "MensPingo Tech Solutions";
	private static final int BREVO_TIMEOUT_MS = 5_000;

	private final RestClient restClient;
	private final String apiKey;
	private final String fromAddress;

	public LeaveNotificationEmailService(
			@Value("${brevo.api-key:}") String apiKey,
			@Value("${mail.from:}") String fromAddress) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(BREVO_TIMEOUT_MS);
		requestFactory.setReadTimeout(BREVO_TIMEOUT_MS);

		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.build();
		this.apiKey = apiKey;
		this.fromAddress = fromAddress;
	}

	public boolean sendDecisionEmail(LeaveRequest leaveRequest) {
		Employee employee = leaveRequest.getEmployee();
		if (employee.getEmail() == null || employee.getEmail().isBlank()) {
			logger.warn("Could not send leave notification for leave {} because employee email is missing.", leaveRequest.getId());
			return false;
		}

		if (apiKey == null || apiKey.isBlank() || fromAddress == null || fromAddress.isBlank()) {
			logger.warn("Could not send leave notification for leave {} because BREVO_API_KEY or MAIL_FROM is missing.", leaveRequest.getId());
			return false;
		}

		try {
			restClient.post()
					.uri(BREVO_EMAIL_ENDPOINT)
					.contentType(MediaType.APPLICATION_JSON)
					.header("accept", MediaType.APPLICATION_JSON_VALUE)
					.header("api-key", apiKey)
					.body(emailPayload(leaveRequest, employee))
					.retrieve()
					.toBodilessEntity();
			return true;
		} catch (RestClientResponseException exception) {
			logger.error(
					"Brevo leave notification failed for leave {} with status {} and response {}.",
					leaveRequest.getId(),
					exception.getStatusCode(),
					exception.getResponseBodyAsString(),
					exception);
			return false;
		} catch (RestClientException exception) {
			logger.error("Brevo leave notification request failed for leave {}.", leaveRequest.getId(), exception);
			return false;
		} catch (Exception exception) {
			logger.error("Unexpected leave notification error for leave {}.", leaveRequest.getId(), exception);
			return false;
		}
	}

	private Map<String, Object> emailPayload(LeaveRequest leaveRequest, Employee employee) {
		String employeeName = employee.getFirstName() + " " + employee.getLastName();

		return Map.of(
				"sender", Map.of(
						"name", COMPANY_NAME,
						"email", fromAddress),
				"to", List.of(Map.of(
						"email", employee.getEmail(),
						"name", employeeName)),
				"subject", subjectFor(leaveRequest.getStatus()),
				"textContent", bodyFor(leaveRequest, employee));
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
