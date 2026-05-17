package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CrmTaskRepository extends JpaRepository<CrmTask, Long> {

	@Override
	@EntityGraph(attributePaths = "customer")
	List<CrmTask> findAll(Sort sort);

	@Override
	@EntityGraph(attributePaths = "customer")
	Optional<CrmTask> findById(Long id);

	@EntityGraph(attributePaths = "customer")
	List<CrmTask> findByFollowUpDate(LocalDate followUpDate, Sort sort);

	@EntityGraph(attributePaths = "customer")
	List<CrmTask> findByFollowUpDateAfter(LocalDate followUpDate, Sort sort);
}
