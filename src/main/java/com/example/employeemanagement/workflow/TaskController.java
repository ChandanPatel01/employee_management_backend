package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@GetMapping("/my")
	public List<TaskResponse> getMyTasks(HttpServletRequest request) {
		return taskService.getMyTasks(authenticatedUserId(request));
	}

	@GetMapping("/team")
	public List<TaskResponse> getTeamTasks(HttpServletRequest request) {
		return taskService.getTeamTasks(authenticatedUserId(request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponse createTask(@Valid @RequestBody TaskCreateRequest request, HttpServletRequest servletRequest) {
		return taskService.createTask(request, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/status")
	public TaskResponse updateStatus(
			@PathVariable("id") Long id,
			@Valid @RequestBody TaskStatusUpdateRequest request,
			HttpServletRequest servletRequest) {
		return taskService.updateStatus(id, request, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/comment")
	public TaskResponse updateComment(
			@PathVariable("id") Long id,
			@RequestBody TaskCommentRequest request,
			HttpServletRequest servletRequest) {
		return taskService.updateComment(id, request, authenticatedUserId(servletRequest));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteTask(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
		taskService.deleteTask(id, authenticatedUserId(servletRequest));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}
		throw new InvalidCredentialsException();
	}
}
