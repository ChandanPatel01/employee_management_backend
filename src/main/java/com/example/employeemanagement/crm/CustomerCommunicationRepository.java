package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerCommunicationRepository extends JpaRepository<CustomerCommunication, Long> {

	@Override
	@EntityGraph(attributePaths = "customer")
	List<CustomerCommunication> findAll(Sort sort);

	@Override
	@EntityGraph(attributePaths = "customer")
	Optional<CustomerCommunication> findById(Long id);

	@EntityGraph(attributePaths = "customer")
	List<CustomerCommunication> findByCustomerId(Long customerId, Sort sort);
}
