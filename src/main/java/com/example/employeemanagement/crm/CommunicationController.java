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
@RequestMapping("/api/crm/communications")
public class CommunicationController {

	private final CommunicationService communicationService;

	public CommunicationController(CommunicationService communicationService) {
		this.communicationService = communicationService;
	}

	@GetMapping
	public List<CommunicationResponse> getCommunications() {
		return communicationService.getCommunications();
	}

	@GetMapping("/customer/{customerId}")
	public List<CommunicationResponse> getCommunicationsByCustomer(@PathVariable("customerId") Long customerId) {
		return communicationService.getCommunicationsByCustomer(customerId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CommunicationResponse createCommunication(@Valid @RequestBody CommunicationRequest request) {
		return communicationService.createCommunication(request);
	}

	@PutMapping("/{id}")
	public CommunicationResponse updateCommunication(@PathVariable("id") Long id, @Valid @RequestBody CommunicationRequest request) {
		return communicationService.updateCommunication(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCommunication(@PathVariable("id") Long id) {
		communicationService.deleteCommunication(id);
	}
}
