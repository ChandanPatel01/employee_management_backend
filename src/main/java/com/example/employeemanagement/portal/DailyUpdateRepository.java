package com.example.employeemanagement.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyUpdateRepository extends JpaRepository<DailyUpdate, Long> {

	List<DailyUpdate> findAllByOrderByUpdateDateDesc();

	List<DailyUpdate> findByUserEmailIgnoreCaseOrderByUpdateDateDesc(String userEmail);
}
