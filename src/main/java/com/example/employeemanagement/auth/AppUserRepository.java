package com.example.employeemanagement.auth;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmployeeId(Long employeeId);

	@Override
	@EntityGraph(attributePaths = "employee")
	List<AppUser> findAll();

	@Override
	@EntityGraph(attributePaths = "employee")
	Optional<AppUser> findById(Long id);

	@EntityGraph(attributePaths = "employee")
	Optional<AppUser> findByEmail(String email);

	@EntityGraph(attributePaths = "employee")
	Optional<AppUser> findByEmployeeId(Long employeeId);

	@EntityGraph(attributePaths = "employee")
	List<AppUser> findByRoleAndEmployeeDepartmentIgnoreCase(UserRole role, String department);
}
