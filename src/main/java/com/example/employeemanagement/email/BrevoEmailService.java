package com.example.employeemanagement.email;

import com.example.employeemanagement.auth.UserRole;
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
public class BrevoEmailService {

	private static final Logger logger = LoggerFactory.getLogger(BrevoEmailService.class);
	private static final String BREVO_EMAIL_ENDPOINT = "https://api.brevo.com/v3/smtp/email";
	private static final String COMPANY_NAME = "MensPingo Tech Solutions";
	private static final String PORTAL_NAME = "MensPingo Employee Management System";
	private static final String ONBOARDING_SUBJECT = "Welcome to MensPingo Employee Management System";
	private static final String DEFAULT_FRONTEND_APP_URL = "https://employee-management-frontends.onrender.com";
	private static final int BREVO_TIMEOUT_MS = 5_000;

	private final RestClient restClient;
	private final String apiKey;
	private final String fromAddress;
	private final String frontendAppUrl;

	public BrevoEmailService(
			@Value("${brevo.api-key:}") String apiKey,
			@Value("${mail.from:}") String fromAddress,
			@Value("${frontend.app-url:" + DEFAULT_FRONTEND_APP_URL + "}") String frontendAppUrl) {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(BREVO_TIMEOUT_MS);
		requestFactory.setReadTimeout(BREVO_TIMEOUT_MS);

		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.build();
		this.apiKey = apiKey;
		this.fromAddress = fromAddress;
		this.frontendAppUrl = cleanOrDefault(frontendAppUrl, DEFAULT_FRONTEND_APP_URL);
	}

	public boolean sendOnboardingEmail(String name, String email, String temporaryPassword, UserRole role) {
		return sendEmail(
				email,
				name,
				ONBOARDING_SUBJECT,
				onboardingBody(name, email, temporaryPassword, role),
				"onboarding email for " + cleanOrDefault(email, "new user"));
	}

	public boolean sendEmail(String toEmail, String toName, String subject, String textContent, String logContext) {
		String context = cleanOrDefault(logContext, "email notification");
		if (toEmail == null || toEmail.isBlank()) {
			logger.warn("Could not send {} because recipient email is missing.", context);
			return false;
		}

		if (apiKey == null || apiKey.isBlank() || fromAddress == null || fromAddress.isBlank()) {
			logger.warn("Could not send {} because BREVO_API_KEY or MAIL_FROM is missing.", context);
			return false;
		}

		try {
			restClient.post()
					.uri(BREVO_EMAIL_ENDPOINT)
					.contentType(MediaType.APPLICATION_JSON)
					.header("accept", MediaType.APPLICATION_JSON_VALUE)
					.header("api-key", apiKey)
					.body(emailPayload(toEmail, toName, subject, textContent))
					.retrieve()
					.toBodilessEntity();
			return true;
		} catch (RestClientResponseException exception) {
			logger.error(
					"Brevo email request failed for {} with status {} and response {}.",
					context,
					exception.getStatusCode(),
					exception.getResponseBodyAsString(),
					exception);
			return false;
		} catch (RestClientException exception) {
			logger.error("Brevo email request failed for {}.", context, exception);
			return false;
		} catch (Exception exception) {
			logger.error("Unexpected Brevo email error for {}.", context, exception);
			return false;
		}
	}

	private Map<String, Object> emailPayload(String toEmail, String toName, String subject, String textContent) {
		return Map.of(
				"sender", Map.of(
						"name", COMPANY_NAME,
						"email", fromAddress),
				"to", List.of(Map.of(
						"email", toEmail.trim(),
						"name", cleanOrDefault(toName, toEmail))),
				"subject", cleanOrDefault(subject, PORTAL_NAME),
				"textContent", cleanOrDefault(textContent, ""));
	}

	private String onboardingBody(String name, String email, String temporaryPassword, UserRole role) {
		String userName = cleanOrDefault(name, "Team Member");
		String loginEmail = cleanOrDefault(email, "-");
		String password = cleanOrDefault(temporaryPassword, "-");
		String roleName = role == null ? UserRole.EMPLOYEE.name() : role.name();

		return """
				Hello %s,

				Congratulations and welcome to %s.

				Your account has been created for the %s.

				Company: %s
				Portal: %s
				Role Assigned: %s
				Login Email: %s
				Temporary Password: %s
				Frontend Login URL: %s

				Please login using the temporary password and change your password immediately after first login.

				Security note: Do not share your password with anyone.
				""".formatted(
				userName,
				COMPANY_NAME,
				PORTAL_NAME,
				COMPANY_NAME,
				PORTAL_NAME,
				roleName,
				loginEmail,
				password,
				frontendAppUrl);
	}

	private String cleanOrDefault(String value, String defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}

		return value.trim();
	}
}
