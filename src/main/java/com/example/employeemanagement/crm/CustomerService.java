package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public List<CustomerResponse> getCustomers() {
		return customerRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
				.stream()
				.map(CustomerResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CustomerResponse getCustomerResponse(Long id) {
		return CustomerResponse.from(getCustomer(id));
	}

	@Transactional(readOnly = true)
	public Customer getCustomer(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new CrmResourceNotFoundException("Customer", id));
	}

	public CustomerResponse createCustomer(CustomerRequest request) {
		Customer customer = new Customer();
		apply(request, customer);
		return CustomerResponse.from(customerRepository.save(customer));
	}

	public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
		Customer customer = getCustomer(id);
		apply(request, customer);
		return CustomerResponse.from(customerRepository.save(customer));
	}

	public void deleteCustomer(Long id) {
		if (!customerRepository.existsById(id)) {
			throw new CrmResourceNotFoundException("Customer", id);
		}

		customerRepository.deleteById(id);
	}

	private void apply(CustomerRequest request, Customer customer) {
		customer.setCompanyName(clean(request.companyName()));
		customer.setContactPerson(clean(request.contactPerson()));
		customer.setEmail(clean(request.email()));
		customer.setPhone(clean(request.phone()));
		customer.setAddress(clean(request.address()));
		customer.setProjectName(clean(request.projectName()));
		customer.setProjectStatus(clean(request.projectStatus()));
		customer.setPaymentStatus(clean(request.paymentStatus()));
		customer.setPaymentHistory(clean(request.paymentHistory()));
		customer.setAssignedTeam(clean(request.assignedTeam()));
		customer.setSupportTickets(clean(request.supportTickets()));
		customer.setPreviousCommunication(clean(request.previousCommunication()));
		customer.setNotes(clean(request.notes()));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
