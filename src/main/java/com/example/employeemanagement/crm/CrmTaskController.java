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
@RequestMapping("/api/crm/tasks")
public class CrmTaskController {

	private final CrmTaskService crmTaskService;

	public CrmTaskController(CrmTaskService crmTaskService) {
		this.crmTaskService = crmTaskService;
	}

	@GetMapping
	public List<CrmTaskResponse> getTasks() {
		return crmTaskService.getTasks();
	}

	@GetMapping("/{id}")
	public CrmTaskResponse getTask(@PathVariable("id") Long id) {
		return crmTaskService.getTask(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CrmTaskResponse createTask(@Valid @RequestBody CrmTaskRequest request) {
		return crmTaskService.createTask(request);
	}

	@PutMapping("/{id}")
	public CrmTaskResponse updateTask(@PathVariable("id") Long id, @Valid @RequestBody CrmTaskRequest request) {
		return crmTaskService.updateTask(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTask(@PathVariable("id") Long id) {
		crmTaskService.deleteTask(id);
	}

	@GetMapping("/today")
	public List<CrmTaskResponse> getTodayTasks() {
		return crmTaskService.getTodayTasks();
	}

	@GetMapping("/upcoming")
	public List<CrmTaskResponse> getUpcomingTasks() {
		return crmTaskService.getUpcomingTasks();
	}
}
