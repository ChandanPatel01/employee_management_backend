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
		Customer customer = communication.getCustomer();
		return new CommunicationResponse(
				communication.getId(),
				customer.getId(),
				customer.getCompanyName(),
				communication.getCommunicationType(),
				communication.getSubject(),
				communication.getSummary(),
				communication.getCommunicationDate(),
				communication.getNextAction(),
				communication.getCreatedBy(),
				communication.getCreatedAt());
	}
}
