package com.example.employeemanagement.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmployeeId(Long employeeId);

	Optional<AppUser> findByEmail(String email);
}
