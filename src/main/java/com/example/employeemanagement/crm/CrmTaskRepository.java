package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CrmTaskRepository extends JpaRepository<CrmTask, Long> {

	List<CrmTask> findByFollowUpDate(LocalDate followUpDate, Sort sort);

	List<CrmTask> findByFollowUpDateAfter(LocalDate followUpDate, Sort sort);
}
