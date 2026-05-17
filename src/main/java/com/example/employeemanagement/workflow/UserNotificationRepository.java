package com.example.employeemanagement.workflow;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	@Override
	@EntityGraph(attributePaths = "user")
	Optional<UserNotification> findById(Long id);

	@EntityGraph(attributePaths = "user")
	List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

	long countByUserIdAndReadFlagFalse(Long userId);
}
