package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.employee.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class DailyUpdateService {

	private final DailyWorkUpdateRepository updateRepository;
	private final AppUserRepository appUserRepository;
	private final NotificationService notificationService;

	public DailyUpdateService(
			DailyWorkUpdateRepository updateRepository,
			AppUserRepository appUserRepository,
			NotificationService notificationService) {
		this.updateRepository = updateRepository;
		this.appUserRepository = appUserRepository;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<DailyUpdateResponse> getMyUpdates(long userId) {
		AppUser actor = actor(userId);
		Employee employee = requireEmployee(actor);
		return updateRepository.findByEmployeeIdOrderByWorkDateDesc(employee.getId()).stream()
				.map(DailyUpdateResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<DailyUpdateResponse> getTeamUpdates(long userId) {
		AppUser actor = actor(userId);
		if (hasFullAccess(actor)) {
			return updateRepository.findAll().stream()
					.map(DailyUpdateResponse::from)
					.toList();
		}

		ensureManager(actor);
		return updateRepository.findByEmployeeDepartmentIgnoreCaseOrderByWorkDateDesc(requireEmployee(actor).getDepartment()).stream()
				.map(DailyUpdateResponse::from)
				.toList();
	}

	public DailyUpdateResponse submitUpdate(DailyUpdateRequest request, long userId) {
		AppUser actor = actor(userId);
		Employee employee = requireEmployee(actor);
		LocalDate workDate = request.workDate() == null ? LocalDate.now() : request.workDate();
		DailyWorkUpdate update = updateRepository.findByEmployeeIdAndWorkDate(employee.getId(), workDate)
				.orElseGet(DailyWorkUpdate::new);
		update.setEmployee(employee);
		update.setWorkDate(workDate);
		update.setUpdateText(requireValue(request.updateText(), "Daily update text is required."));
		update.setBlockers(clean(request.blockers()));
		update.setStatus(DailyUpdateStatus.SUBMITTED);
		update.setManagerComment(null);
		update.setReviewedBy(null);
		update.setReviewedAt(null);

		DailyWorkUpdate savedUpdate = updateRepository.save(update);
		notifyManagers(employee, "Daily update submitted", "%s submitted a daily update.".formatted(actor.getName()));
		return DailyUpdateResponse.from(savedUpdate);
	}

	public DailyUpdateResponse reviewUpdate(Long id, DailyUpdateReviewRequest request, long userId) {
		AppUser actor = actor(userId);
		ensureCanReview(actor);
		DailyWorkUpdate update = updateRepository.findById(id)
				.orElseThrow(() -> new WorkflowException("Daily update not found."));
		ensureCanReviewEmployee(actor, update.getEmployee());

		update.setManagerComment(clean(request.managerComment()));
		update.setReviewedBy(actor);
		update.setReviewedAt(Instant.now());
		update.setStatus(DailyUpdateStatus.REVIEWED);

		DailyWorkUpdate savedUpdate = updateRepository.save(update);
		notificationService.notifyEmployee(
				savedUpdate.getEmployee().getId(),
				"Daily update reviewed",
				"%s reviewed your daily update.".formatted(actor.getName()),
				NotificationType.DAILY_UPDATE_REVIEWED);
		return DailyUpdateResponse.from(savedUpdate);
	}

	private void notifyManagers(Employee employee, String title, String message) {
		appUserRepository.findByRoleAndEmployeeDepartmentIgnoreCase(UserRole.MANAGER, employee.getDepartment())
				.forEach((manager) -> notificationService.notifyUser(manager, title, message, NotificationType.DAILY_UPDATE_SUBMITTED));
	}

	private AppUser actor(long userId) {
		return appUserRepository.findById(userId)
				.orElseThrow(() -> new WorkflowException("Authenticated user was not found."));
	}

	private Employee requireEmployee(AppUser user) {
		if (user.getEmployee() == null) {
			throw new WorkflowException("Your login account is not linked to an employee profile.");
		}
		return user.getEmployee();
	}

	private void ensureManager(AppUser user) {
		if (user.getRole() == UserRole.MANAGER) {
			return;
		}
		throw new WorkflowException("Only managers can view team updates.");
	}

	private void ensureCanReview(AppUser user) {
		if (hasFullAccess(user) || user.getRole() == UserRole.MANAGER) {
			return;
		}
		throw new WorkflowException("You do not have permission to review daily updates.");
	}

	private void ensureCanReviewEmployee(AppUser actor, Employee employee) {
		if (hasFullAccess(actor)) {
			return;
		}
		Employee managerEmployee = requireEmployee(actor);
		if (actor.getRole() == UserRole.MANAGER
				&& managerEmployee.getDepartment() != null
				&& employee.getDepartment() != null
				&& managerEmployee.getDepartment().equalsIgnoreCase(employee.getDepartment())) {
			return;
		}
		throw new WorkflowException("You can review only your team updates.");
	}

	private boolean hasFullAccess(AppUser user) {
		return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FOUNDER;
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
