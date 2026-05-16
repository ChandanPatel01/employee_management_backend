package com.example.employeemanagement.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {

	List<TaskItem> findAllByOrderByDueDateAsc();

	List<TaskItem> findByAssignedToIgnoreCaseOrderByDueDateAsc(String assignedTo);

	List<TaskItem> findByDueDateOrderByDueDateAsc(LocalDate dueDate);

	List<TaskItem> findByDueDateGreaterThanEqualOrderByDueDateAsc(LocalDate dueDate);
}
