package com.example.employeemanagement.crm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crm_customers")
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Column(nullable = false)
	private String companyName;

	@NotBlank
	@Column(nullable = false)
	private String contactPerson;

	@NotBlank
	@Email
	@Column(nullable = false)
	private String email;

	private String phone;

	@Column(length = 1000)
	private String address;

	private String projectName;

	private String projectStatus;

	private String paymentStatus;

	@Column(length = 4000)
	private String paymentHistory;

	private String assignedTeam;

	@Column(length = 4000)
	private String supportTickets;

	@Column(length = 4000)
	private String previousCommunication;

	@Column(length = 4000)
	private String notes;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@JsonIgnore
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CrmTask> tasks = new ArrayList<>();

	@JsonIgnore
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CustomerCommunication> communications = new ArrayList<>();

	public Customer() {
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(String projectStatus) {
		this.projectStatus = projectStatus;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentHistory() {
		return paymentHistory;
	}

	public void setPaymentHistory(String paymentHistory) {
		this.paymentHistory = paymentHistory;
	}

	public String getAssignedTeam() {
		return assignedTeam;
	}

	public void setAssignedTeam(String assignedTeam) {
		this.assignedTeam = assignedTeam;
	}

	public String getSupportTickets() {
		return supportTickets;
	}

	public void setSupportTickets(String supportTickets) {
		this.supportTickets = supportTickets;
	}

	public String getPreviousCommunication() {
		return previousCommunication;
	}

	public void setPreviousCommunication(String previousCommunication) {
		this.previousCommunication = previousCommunication;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
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

	public List<CrmTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<CrmTask> tasks) {
		this.tasks = tasks;
	}

	public List<CustomerCommunication> getCommunications() {
		return communications;
	}

	public void setCommunications(List<CustomerCommunication> communications) {
		this.communications = communications;
	}
}
