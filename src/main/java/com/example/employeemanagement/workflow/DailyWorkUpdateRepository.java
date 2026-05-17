package com.example.employeemanagement.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyWorkUpdateRepository extends JpaRepository<DailyWorkUpdate, Long> {

	List<DailyWorkUpdate> findByEmployeeIdOrderByWorkDateDesc(Long employeeId);

	List<DailyWorkUpdate> findByEmployeeDepartmentIgnoreCaseOrderByWorkDateDesc(String department);

	Optional<DailyWorkUpdate> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}
