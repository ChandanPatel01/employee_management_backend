package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.employee.Employee;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LeaveRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
	private Employee employee;

	@NotBlank
	@Column(nullable = false)
	private String leaveType;

	@NotNull
	@Column(nullable = false)
	private LocalDate fromDate;

	@NotNull
	@Column(nullable = false)
	private LocalDate toDate;

	@Column(length = 1000)
	private String description;

	@NotNull
	@Column(nullable = false)
	private LocalDate appliedDate = LocalDate.now();

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LeaveStatus status = LeaveStatus.PENDING;

	@Column(length = 1000)
	private String decisionReason;

	private Instant decidedAt;

	public LeaveRequest() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public String getLeaveType() {
		return leaveType;
	}

	public void setLeaveType(String leaveType) {
		this.leaveType = leaveType;
	}

	public LocalDate getFromDate() {
		return fromDate;
	}

	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}

	public LocalDate getToDate() {
		return toDate;
	}

	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getAppliedDate() {
		return appliedDate;
	}

	public void setAppliedDate(LocalDate appliedDate) {
		this.appliedDate = appliedDate;
	}

	public LeaveStatus getStatus() {
		return status;
	}

	public void setStatus(LeaveStatus status) {
		this.status = status;
	}

	public String getDecisionReason() {
		return decisionReason;
	}

	public void setDecisionReason(String decisionReason) {
		this.decisionReason = decisionReason;
	}

	public Instant getDecidedAt() {
		return decidedAt;
	}

	public void setDecidedAt(Instant decidedAt) {
		this.decidedAt = decidedAt;
	}
}
