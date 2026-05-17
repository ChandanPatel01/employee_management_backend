package com.example.employeemanagement.audit;

import com.example.employeemanagement.auth.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 80)
	private String action;

	private Long actorUserId;

	private String actorEmail;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private UserRole actorRole;

	private Long targetEmployeeId;

	@Column(length = 32)
	private String targetEmployeeCode;

	private String targetEmployeeEmail;

	private String targetEmployeeName;

	@Column(nullable = false, length = 2000)
	private String details;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getActorUserId() {
		return actorUserId;
	}

	public void setActorUserId(Long actorUserId) {
		this.actorUserId = actorUserId;
	}

	public String getActorEmail() {
		return actorEmail;
	}

	public void setActorEmail(String actorEmail) {
		this.actorEmail = actorEmail;
	}

	public UserRole getActorRole() {
		return actorRole;
	}

	public void setActorRole(UserRole actorRole) {
		this.actorRole = actorRole;
	}

	public Long getTargetEmployeeId() {
		return targetEmployeeId;
	}

	public void setTargetEmployeeId(Long targetEmployeeId) {
		this.targetEmployeeId = targetEmployeeId;
	}

	public String getTargetEmployeeCode() {
		return targetEmployeeCode;
	}

	public void setTargetEmployeeCode(String targetEmployeeCode) {
		this.targetEmployeeCode = targetEmployeeCode;
	}

	public String getTargetEmployeeEmail() {
		return targetEmployeeEmail;
	}

	public void setTargetEmployeeEmail(String targetEmployeeEmail) {
		this.targetEmployeeEmail = targetEmployeeEmail;
	}

	public String getTargetEmployeeName() {
		return targetEmployeeName;
	}

	public void setTargetEmployeeName(String targetEmployeeName) {
		this.targetEmployeeName = targetEmployeeName;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
