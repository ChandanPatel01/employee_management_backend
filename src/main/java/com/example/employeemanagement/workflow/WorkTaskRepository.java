package com.example.employeemanagement.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {

	List<WorkTask> findByAssignedToEmployeeIdOrderByCreatedAtDesc(Long employeeId);

	List<WorkTask> findByAssignedToEmployeeDepartmentIgnoreCaseOrderByCreatedAtDesc(String department);
}
