package com.example.employeemanagement.workflow;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {

	@Override
	@EntityGraph(attributePaths = {"assignedToEmployee", "assignedByUser"})
	List<WorkTask> findAll();

	@Override
	@EntityGraph(attributePaths = {"assignedToEmployee", "assignedByUser"})
	Optional<WorkTask> findById(Long id);

	@EntityGraph(attributePaths = {"assignedToEmployee", "assignedByUser"})
	List<WorkTask> findByAssignedToEmployeeIdOrderByCreatedAtDesc(Long employeeId);

	@EntityGraph(attributePaths = {"assignedToEmployee", "assignedByUser"})
	List<WorkTask> findByAssignedToEmployeeDepartmentIgnoreCaseOrderByCreatedAtDesc(String department);
}
