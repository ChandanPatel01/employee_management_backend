package com.example.employeemanagement.portal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private final TaskService taskService;
	private final ProjectService projectService;

	public ReportController(TaskService taskService, ProjectService projectService) {
		this.taskService = taskService;
		this.projectService = projectService;
	}

	@GetMapping("/task-summary")
	public TaskSummaryResponse getTaskSummary() {
		List<TaskItem> tasks = taskService.getAllTasks();
		return new TaskSummaryResponse(
				tasks.size(),
				countByStatus(tasks, "PENDING"),
				countByStatus(tasks, "IN_PROGRESS"),
				countByStatus(tasks, "COMPLETED"),
				tasks.stream().filter(task -> "HIGH".equalsIgnoreCase(task.getPriority())).count());
	}

	@GetMapping("/team-progress")
	public List<Project> getTeamProgress() {
		return projectService.getProjects();
	}

	private long countByStatus(List<TaskItem> tasks, String status) {
		return tasks.stream()
				.filter(task -> status.equalsIgnoreCase(task.getStatus()))
				.count();
	}
}
