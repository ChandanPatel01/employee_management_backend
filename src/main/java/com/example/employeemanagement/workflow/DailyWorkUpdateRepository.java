package com.example.employeemanagement.workflow;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyWorkUpdateRepository extends JpaRepository<DailyWorkUpdate, Long> {

	@Override
	@EntityGraph(attributePaths = {"employee", "reviewedBy"})
	List<DailyWorkUpdate> findAll();

	@Override
	@EntityGraph(attributePaths = {"employee", "reviewedBy"})
	Optional<DailyWorkUpdate> findById(Long id);

	@EntityGraph(attributePaths = {"employee", "reviewedBy"})
	List<DailyWorkUpdate> findByEmployeeIdOrderByWorkDateDesc(Long employeeId);

	@EntityGraph(attributePaths = {"employee", "reviewedBy"})
	List<DailyWorkUpdate> findByEmployeeDepartmentIgnoreCaseOrderByWorkDateDesc(String department);

	@EntityGraph(attributePaths = {"employee", "reviewedBy"})
	Optional<DailyWorkUpdate> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}
