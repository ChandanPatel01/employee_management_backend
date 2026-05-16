package com.example.employeemanagement.crm;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CrmTaskService {

	private final CrmTaskRepository crmTaskRepository;
	private final CustomerService customerService;

	public CrmTaskService(CrmTaskRepository crmTaskRepository, CustomerService customerService) {
		this.crmTaskRepository = crmTaskRepository;
		this.customerService = customerService;
	}

	@Transactional(readOnly = true)
	public List<CrmTaskResponse> getTasks() {
		return crmTaskRepository.findAll(taskSort())
				.stream()
				.map(CrmTaskResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public CrmTaskResponse getTask(Long id) {
		return CrmTaskResponse.from(findTask(id));
	}

	@Transactional(readOnly = true)
	public List<CrmTaskResponse> getTodayTasks() {
		return crmTaskRepository.findByFollowUpDate(LocalDate.now(), taskSort())
				.stream()
				.map(CrmTaskResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<CrmTaskResponse> getUpcomingTasks() {
		return crmTaskRepository.findByFollowUpDateAfter(LocalDate.now(), taskSort())
				.stream()
				.map(CrmTaskResponse::from)
				.toList();
	}

	public CrmTaskResponse createTask(CrmTaskRequest request) {
		CrmTask task = new CrmTask();
		apply(request, task);
		return CrmTaskResponse.from(crmTaskRepository.save(task));
	}

	public CrmTaskResponse updateTask(Long id, CrmTaskRequest request) {
		CrmTask task = findTask(id);
		apply(request, task);
		return CrmTaskResponse.from(crmTaskRepository.save(task));
	}

	public void deleteTask(Long id) {
		if (!crmTaskRepository.existsById(id)) {
			throw new CrmResourceNotFoundException("CRM task", id);
		}

		crmTaskRepository.deleteById(id);
	}

	private CrmTask findTask(Long id) {
		return crmTaskRepository.findById(id)
				.orElseThrow(() -> new CrmResourceNotFoundException("CRM task", id));
	}

	private void apply(CrmTaskRequest request, CrmTask task) {
		Customer customer = customerService.getCustomer(request.customerId());
		task.setCustomer(customer);
		task.setTitle(clean(request.title()));
		task.setDescription(clean(request.description()));
		task.setFollowUpDate(request.followUpDate());
		task.setFollowUpTime(request.followUpTime());
		task.setPriority(clean(request.priority()));
		task.setStatus(clean(request.status()));
		task.setAssignedTo(clean(request.assignedTo()));
		task.setReminderType(clean(request.reminderType()));
	}

	private Sort taskSort() {
		return Sort.by(Sort.Order.asc("followUpDate"), Sort.Order.asc("followUpTime"), Sort.Order.desc("createdAt"));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
