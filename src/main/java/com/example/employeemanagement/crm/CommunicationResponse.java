package com.example.employeemanagement.crm;

import java.time.Instant;
import java.time.LocalDateTime;

public record CommunicationResponse(
		Long id,
		Long customerId,
		String customerName,
		CommunicationType communicationType,
		String subject,
		String summary,
		LocalDateTime communicationDate,
		String nextAction,
		String createdBy,
		Instant createdAt
) {
	public static CommunicationResponse from(CustomerCommunication communication) {
		Customer customer = communication == null ? null : communication.getCustomer();
		return new CommunicationResponse(
				communication == null ? null : communication.getId(),
				customer == null ? null : customer.getId(),
				customer == null ? null : customer.getCompanyName(),
				communication == null ? null : communication.getCommunicationType(),
				communication == null ? null : communication.getSubject(),
				communication == null ? null : communication.getSummary(),
				communication == null ? null : communication.getCommunicationDate(),
				communication == null ? null : communication.getNextAction(),
				communication == null ? null : communication.getCreatedBy(),
				communication == null ? null : communication.getCreatedAt());
	}
}
