package com.example.employeemanagement.portal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

	List<Candidate> findAllByOrderByCreatedAtDesc();
}
