package com.example.employeemanagement.portal;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/team")
public class TeamLeadController {

	private final TaskService taskService;
	private final DailyUpdateService dailyUpdateService;

	public TeamLeadController(TaskService taskService, DailyUpdateService dailyUpdateService) {
		this.taskService = taskService;
		this.dailyUpdateService = dailyUpdateService;
	}

	@GetMapping("/tasks")
	public List<TaskItem> getTeamTasks() {
		return taskService.getAllTasks();
	}

	@PostMapping("/tasks")
	@ResponseStatus(HttpStatus.CREATED)
	public TaskItem createTeamTask(HttpServletRequest request, @RequestBody TaskItem task) {
		return taskService.createTask(task, currentEmail(request));
	}

	@PutMapping("/tasks/{id}")
	public TaskItem updateTeamTask(HttpServletRequest request, @PathVariable("id") Long id, @RequestBody TaskItem task) {
		return taskService.updateTask(id, task, currentEmail(request));
	}

	@DeleteMapping("/tasks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTeamTask(@PathVariable("id") Long id) {
		taskService.deleteTask(id);
	}

	@GetMapping("/updates")
	public List<DailyUpdate> getTeamUpdates() {
		return dailyUpdateService.getAllUpdates();
	}

	@GetMapping("/performance")
	public List<PerformanceSummaryResponse> getEmployeePerformance() {
		List<TaskItem> tasks = taskService.getAllTasks();
		Map<String, Long> updateCounts = dailyUpdateService.getAllUpdates().stream()
				.collect(Collectors.groupingBy(DailyUpdate::getUserEmail, Collectors.counting()));

		return tasks.stream()
				.collect(Collectors.groupingBy(TaskItem::getAssignedTo))
				.entrySet()
				.stream()
				.map(entry -> {
					long total = entry.getValue().size();
					long completed = entry.getValue().stream()
							.filter(task -> "COMPLETED".equalsIgnoreCase(task.getStatus()))
							.count();
					double completionRate = total == 0 ? 0 : (completed * 100.0) / total;
					return new PerformanceSummaryResponse(
							entry.getKey(),
							total,
							completed,
							updateCounts.getOrDefault(entry.getKey(), 0L),
							completionRate);
				})
				.toList();
	}

	private String currentEmail(HttpServletRequest request) {
		Object email = request.getAttribute("authenticatedUserEmail");
		return email == null ? "" : email.toString();
	}
}
