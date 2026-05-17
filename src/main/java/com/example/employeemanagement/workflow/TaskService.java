package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class TaskService {

	private final WorkTaskRepository taskRepository;
	private final AppUserRepository appUserRepository;
	private final EmployeeService employeeService;
	private final NotificationService notificationService;

	public TaskService(
			WorkTaskRepository taskRepository,
			AppUserRepository appUserRepository,
			EmployeeService employeeService,
			NotificationService notificationService) {
		this.taskRepository = taskRepository;
		this.appUserRepository = appUserRepository;
		this.employeeService = employeeService;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> getMyTasks(long userId) {
		AppUser actor = actor(userId);
		Employee employee = requireEmployee(actor);
		return taskRepository.findByAssignedToEmployeeIdOrderByCreatedAtDesc(employee.getId()).stream()
				.map(TaskResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> getTeamTasks(long userId) {
		AppUser actor = actor(userId);
		if (hasFullAccess(actor)) {
			return taskRepository.findAll().stream()
					.map(TaskResponse::from)
					.toList();
		}

		ensureManager(actor);
		return taskRepository.findByAssignedToEmployeeDepartmentIgnoreCaseOrderByCreatedAtDesc(requireEmployee(actor).getDepartment()).stream()
				.map(TaskResponse::from)
				.toList();
	}

	public TaskResponse createTask(TaskCreateRequest request, long userId) {
		AppUser actor = actor(userId);
		ensureCanManageTasks(actor);
		Employee assignee = employeeService.getEmployee(request.assignedToEmployeeId());
		ensureCanManageEmployee(actor, assignee);

		WorkTask task = new WorkTask();
		task.setTitle(requireValue(request.title(), "Task title is required."));
		task.setDescription(clean(request.description()));
		task.setAssignedToEmployee(assignee);
		task.setAssignedByUser(actor);
		task.setPriority(request.priority());
		task.setDeadline(request.deadline());
		task.setUpdatedAt(Instant.now());

		WorkTask savedTask = taskRepository.save(task);
		notificationService.notifyEmployee(
				assignee.getId(),
				"New task assigned",
				"%s assigned you: %s".formatted(actor.getName(), savedTask.getTitle()),
				NotificationType.TASK_ASSIGNED);
		return TaskResponse.from(savedTask);
	}

	public TaskResponse updateStatus(Long id, TaskStatusUpdateRequest request, long userId) {
		AppUser actor = actor(userId);
		WorkTask task = task(id);
		ensureCanUpdateTaskStatus(actor, task);
		task.setStatus(request.status());
		task.setProgressNote(clean(request.progressNote()));
		task.setUpdatedAt(Instant.now());

		WorkTask savedTask = taskRepository.save(task);
		notifyTaskOwnerAndAssigner(savedTask, actor, "Task updated", "Task '%s' is now %s.".formatted(savedTask.getTitle(), savedTask.getStatus()));
		return TaskResponse.from(savedTask);
	}

	public TaskResponse updateComment(Long id, TaskCommentRequest request, long userId) {
		AppUser actor = actor(userId);
		ensureCanManageTasks(actor);
		WorkTask task = task(id);
		ensureCanManageEmployee(actor, task.getAssignedToEmployee());

		if (request.managerComment() != null) {
			task.setManagerComment(clean(request.managerComment()));
		}
		if (request.priority() != null) {
			task.setPriority(request.priority());
		}
		if (request.deadline() != null) {
			task.setDeadline(request.deadline());
		}
		if (request.status() != null) {
			task.setStatus(request.status());
		}
		task.setUpdatedAt(Instant.now());

		WorkTask savedTask = taskRepository.save(task);
		notificationService.notifyEmployee(
				savedTask.getAssignedToEmployee().getId(),
				"Manager comment added",
				"%s updated task '%s'.".formatted(actor.getName(), savedTask.getTitle()),
				NotificationType.TASK_UPDATED);
		return TaskResponse.from(savedTask);
	}

	public void deleteTask(Long id, long userId) {
		AppUser actor = actor(userId);
		ensureCanManageTasks(actor);
		WorkTask task = task(id);
		ensureCanManageEmployee(actor, task.getAssignedToEmployee());
		taskRepository.delete(task);
	}

	private AppUser actor(long userId) {
		return appUserRepository.findById(userId)
				.orElseThrow(() -> new WorkflowException("Authenticated user was not found."));
	}

	private WorkTask task(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> new WorkflowException("Task not found."));
	}

	private Employee requireEmployee(AppUser user) {
		if (user.getEmployee() == null) {
			throw new WorkflowException("Your login account is not linked to an employee profile.");
		}
		return user.getEmployee();
	}

	private void ensureCanManageTasks(AppUser user) {
		if (hasFullAccess(user) || user.getRole() == UserRole.MANAGER) {
			return;
		}
		throw new WorkflowException("You do not have permission to manage tasks.");
	}

	private void ensureManager(AppUser user) {
		if (user.getRole() == UserRole.MANAGER) {
			return;
		}
		throw new WorkflowException("Only managers can view team tasks.");
	}

	private void ensureCanManageEmployee(AppUser actor, Employee employee) {
		if (hasFullAccess(actor)) {
			return;
		}
		Employee managerEmployee = requireEmployee(actor);
		if (actor.getRole() == UserRole.MANAGER && sameDepartment(managerEmployee, employee)) {
			return;
		}
		throw new WorkflowException("You can manage only your team tasks.");
	}

	private void ensureCanUpdateTaskStatus(AppUser actor, WorkTask task) {
		if (hasFullAccess(actor)) {
			return;
		}
		if (actor.getRole() == UserRole.MANAGER && sameDepartment(requireEmployee(actor), task.getAssignedToEmployee())) {
			return;
		}
		if (task.getAssignedToEmployee().getId().equals(requireEmployee(actor).getId())) {
			return;
		}
		throw new WorkflowException("You can update only your own tasks.");
	}

	private boolean hasFullAccess(AppUser user) {
		return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FOUNDER;
	}

	private boolean sameDepartment(Employee left, Employee right) {
		return left.getDepartment() != null && right.getDepartment() != null
				&& left.getDepartment().equalsIgnoreCase(right.getDepartment());
	}

	private void notifyTaskOwnerAndAssigner(WorkTask task, AppUser actor, String title, String message) {
		notificationService.notifyEmployee(task.getAssignedToEmployee().getId(), title, message, NotificationType.TASK_UPDATED);
		if (!task.getAssignedByUser().getId().equals(actor.getId())) {
			notificationService.notifyUser(task.getAssignedByUser(), title, message, NotificationType.TASK_UPDATED);
		}
	}

	private String requireValue(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new WorkflowException(message);
		}
		return value.trim();
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
