package com.example.employeemanagement.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmployeeId(Long employeeId);

	Optional<AppUser> findByEmail(String email);

	Optional<AppUser> findByEmployeeId(Long employeeId);

	List<AppUser> findByRoleAndEmployeeDepartmentIgnoreCase(UserRole role, String department);
}
