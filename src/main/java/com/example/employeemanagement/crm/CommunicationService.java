package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommunicationService {

	private final CustomerCommunicationRepository communicationRepository;
	private final CustomerService customerService;

	public CommunicationService(CustomerCommunicationRepository communicationRepository, CustomerService customerService) {
		this.communicationRepository = communicationRepository;
		this.customerService = customerService;
	}

	@Transactional(readOnly = true)
	public List<CommunicationResponse> getCommunications() {
		return communicationRepository.findAll(communicationSort())
				.stream()
				.map(CommunicationResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CommunicationResponse> getCommunicationsByCustomer(Long customerId) {
		customerService.getCustomer(customerId);
		return communicationRepository.findByCustomerId(customerId, communicationSort())
				.stream()
				.map(CommunicationResponse::from)
				.toList();
	}

	public CommunicationResponse createCommunication(CommunicationRequest request) {
		CustomerCommunication communication = new CustomerCommunication();
		apply(request, communication);
		return CommunicationResponse.from(communicationRepository.save(communication));
	}

	public CommunicationResponse updateCommunication(Long id, CommunicationRequest request) {
		CustomerCommunication communication = findCommunication(id);
		apply(request, communication);
		return CommunicationResponse.from(communicationRepository.save(communication));
	}

	public void deleteCommunication(Long id) {
		if (!communicationRepository.existsById(id)) {
			throw new CrmResourceNotFoundException("Communication", id);
		}

		communicationRepository.deleteById(id);
	}

	private CustomerCommunication findCommunication(Long id) {
		return communicationRepository.findById(id)
				.orElseThrow(() -> new CrmResourceNotFoundException("Communication", id));
	}

	private void apply(CommunicationRequest request, CustomerCommunication communication) {
		Customer customer = customerService.getCustomer(request.customerId());
		communication.setCustomer(customer);
		communication.setCommunicationType(request.communicationType());
		communication.setSubject(clean(request.subject()));
		communication.setSummary(clean(request.summary()));
		communication.setCommunicationDate(request.communicationDate());
		communication.setNextAction(clean(request.nextAction()));
		communication.setCreatedBy(clean(request.createdBy()));
	}

	private Sort communicationSort() {
		return Sort.by(Sort.Order.desc("communicationDate"), Sort.Order.desc("createdAt"));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
