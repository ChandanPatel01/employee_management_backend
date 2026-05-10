package com.example.employeemanagement.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, Long id);

	Optional<Employee> findByEmail(String email);

	List<Employee> findByDepartmentIgnoreCase(String department);
}
