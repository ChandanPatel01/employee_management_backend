package com.example.employeemanagement.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

	List<EmployeeDocument> findAllByOrderByUploadedAtDesc();

	List<EmployeeDocument> findByEmployeeIdOrderByUploadedAtDesc(Long employeeId);
}
