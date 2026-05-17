package com.example.employeemanagement.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

	List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

	long countByUserIdAndReadFalse(Long userId);
}
