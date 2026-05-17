package com.example.employeemanagement.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByEmployeeCode(String employeeCode);

	Optional<Employee> findByEmail(String email);

	Optional<Employee> findByEmailIgnoreCase(String email);

	List<Employee> findByStatus(EmploymentStatus status);

	List<Employee> findByDepartmentIgnoreCase(String department);

	List<Employee> findByStatusAndDepartmentIgnoreCase(EmploymentStatus status, String department);
}
