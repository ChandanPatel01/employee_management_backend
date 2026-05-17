package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.employee.Employee;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "work_tasks")
public class WorkTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(length = 2000)
	private String description;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_to_employee_id", nullable = false)
	private Employee assignedToEmployee;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assigned_by_user_id", nullable = false)
	private AppUser assignedByUser;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskPriority priority = TaskPriority.MEDIUM;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskStatus status = TaskStatus.TODO;

	@Column(length = 2000)
	private String progressNote;

	@Column(length = 2000)
	private String managerComment;

	private LocalDate deadline;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	@Column(nullable = false)
	private Instant updatedAt = Instant.now();

	@PreUpdate
	public void touch() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Employee getAssignedToEmployee() {
		return assignedToEmployee;
	}

	public void setAssignedToEmployee(Employee assignedToEmployee) {
		this.assignedToEmployee = assignedToEmployee;
	}

	public AppUser getAssignedByUser() {
		return assignedByUser;
	}

	public void setAssignedByUser(AppUser assignedByUser) {
		this.assignedByUser = assignedByUser;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public void setPriority(TaskPriority priority) {
		this.priority = priority == null ? TaskPriority.MEDIUM : priority;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status == null ? TaskStatus.TODO : status;
	}

	public String getProgressNote() {
		return progressNote;
	}

	public void setProgressNote(String progressNote) {
		this.progressNote = progressNote;
	}

	public String getManagerComment() {
		return managerComment;
	}

	public void setManagerComment(String managerComment) {
		this.managerComment = managerComment;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDate deadline) {
		this.deadline = deadline;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
