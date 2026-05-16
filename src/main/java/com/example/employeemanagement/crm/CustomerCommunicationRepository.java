package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerCommunicationRepository extends JpaRepository<CustomerCommunication, Long> {

	List<CustomerCommunication> findByCustomerId(Long customerId, Sort sort);
}
