package com.example.employeemanagement.crm;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crm/customers")
public class CustomerController {

	private final CustomerService customerService;

	public CustomerController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@GetMapping
	public List<CustomerResponse> getCustomers() {
		return customerService.getCustomers();
	}

	@GetMapping("/{id}")
	public CustomerResponse getCustomer(@PathVariable("id") Long id) {
		return customerService.getCustomerResponse(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest request) {
		return customerService.createCustomer(request);
	}

	@PutMapping("/{id}")
	public CustomerResponse updateCustomer(@PathVariable("id") Long id, @Valid @RequestBody CustomerRequest request) {
		return customerService.updateCustomer(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCustomer(@PathVariable("id") Long id) {
		customerService.deleteCustomer(id);
	}
}
