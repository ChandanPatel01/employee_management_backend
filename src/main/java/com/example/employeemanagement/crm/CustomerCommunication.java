package com.example.employeemanagement.crm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "crm_communications")
public class CustomerCommunication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private CommunicationType communicationType;

	@NotBlank
	@Column(nullable = false)
	private String subject;

	@NotBlank
	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String summary;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime communicationDate;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String nextAction;

	private String createdBy;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	public CustomerCommunication() {
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public CommunicationType getCommunicationType() {
		return communicationType;
	}

	public void setCommunicationType(CommunicationType communicationType) {
		this.communicationType = communicationType;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public LocalDateTime getCommunicationDate() {
		return communicationDate;
	}

	public void setCommunicationDate(LocalDateTime communicationDate) {
		this.communicationDate = communicationDate;
	}

	public String getNextAction() {
		return nextAction;
	}

	public void setNextAction(String nextAction) {
		this.nextAction = nextAction;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
