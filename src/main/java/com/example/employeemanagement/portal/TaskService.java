package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class TaskService {

	private final TaskItemRepository taskItemRepository;

	public TaskService(TaskItemRepository taskItemRepository) {
		this.taskItemRepository = taskItemRepository;
	}

	@Transactional(readOnly = true)
	public List<TaskItem> getAllTasks() {
		return taskItemRepository.findAllByOrderByDueDateAsc();
	}

	@Transactional(readOnly = true)
	public List<TaskItem> getTasksForUser(String email) {
		return taskItemRepository.findByAssignedToIgnoreCaseOrderByDueDateAsc(email);
	}

	@Transactional(readOnly = true)
	public List<TaskItem> getTodayTasks() {
		return taskItemRepository.findByDueDateOrderByDueDateAsc(LocalDate.now());
	}

	@Transactional(readOnly = true)
	public List<TaskItem> getUpcomingTasks() {
		return taskItemRepository.findByDueDateGreaterThanEqualOrderByDueDateAsc(LocalDate.now().plusDays(1));
	}

	public TaskItem createTask(TaskItem request, String assignedBy) {
		TaskItem task = new TaskItem();
		apply(request, task, assignedBy);
		return taskItemRepository.save(task);
	}

	public TaskItem updateTask(Long id, TaskItem request, String assignedBy) {
		TaskItem task = taskItemRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Task", id));
		apply(request, task, assignedBy);
		return taskItemRepository.save(task);
	}

	public void deleteTask(Long id) {
		if (!taskItemRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Task", id);
		}

		taskItemRepository.deleteById(id);
	}

	private void apply(TaskItem source, TaskItem target, String assignedBy) {
		target.setTitle(required(source.getTitle(), "Untitled task"));
		target.setDescription(clean(source.getDescription()));
		target.setPriority(defaulted(source.getPriority(), "MEDIUM").toUpperCase());
		target.setStatus(defaulted(source.getStatus(), "PENDING").toUpperCase());
		target.setDueDate(source.getDueDate() == null ? LocalDate.now() : source.getDueDate());
		target.setAssignedBy(defaulted(source.getAssignedBy(), assignedBy));
		target.setAssignedTo(required(source.getAssignedTo(), assignedBy));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String defaulted(String value, String fallback) {
		String cleaned = clean(value);
		return cleaned == null ? fallback : cleaned;
	}

	private String required(String value, String fallback) {
		return defaulted(value, fallback);
	}
}
