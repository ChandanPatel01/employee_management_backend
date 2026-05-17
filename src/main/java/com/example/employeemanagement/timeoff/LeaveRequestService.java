package com.example.employeemanagement.timeoff;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.UserRole;
import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmployeeService;
import com.example.employeemanagement.workflow.NotificationService;
import com.example.employeemanagement.workflow.NotificationType;
import com.example.employeemanagement.workflow.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LeaveRequestService {

	private static final Logger logger = LoggerFactory.getLogger(LeaveRequestService.class);
	private static final String EMAIL_SENT_MESSAGE = "Leave status updated and notification email sent.";
	private static final String EMAIL_FAILED_MESSAGE = "Leave status updated, but email notification could not be sent.";

	private final LeaveRequestRepository leaveRequestRepository;
	private final EmployeeService employeeService;
	private final LeaveNotificationEmailService leaveNotificationEmailService;
	private final AppUserRepository appUserRepository;
	private final NotificationService notificationService;

	public LeaveRequestService(
			LeaveRequestRepository leaveRequestRepository,
			EmployeeService employeeService,
			LeaveNotificationEmailService leaveNotificationEmailService,
			AppUserRepository appUserRepository,
			NotificationService notificationService) {
		this.leaveRequestRepository = leaveRequestRepository;
		this.employeeService = employeeService;
		this.leaveNotificationEmailService = leaveNotificationEmailService;
		this.appUserRepository = appUserRepository;
		this.notificationService = notificationService;
	}

	@Transactional(readOnly = true)
	public List<LeaveResponse> getLeaves(LeaveStatus status, Long employeeId, long actorId) {
		AppUser actor = actor(actorId);
		if (!hasPeopleAccess(actor)) {
			Employee actorEmployee = requireEmployee(actor);
			if (actor.getRole() == UserRole.MANAGER) {
				return scopedLeaves(status, employeeId).stream()
						.filter((leave) -> sameDepartment(actorEmployee, leave.getEmployee()))
						.map(LeaveResponse::from)
						.toList();
			}

			return toResponses(scopedLeaves(status, actorEmployee.getId()));
		}

		return toResponses(scopedLeaves(status, employeeId));
	}

	private List<LeaveRequest> scopedLeaves(LeaveStatus status, Long employeeId) {
		if (status != null && employeeId != null) {
			return leaveRequestRepository.findByStatusAndEmployeeId(status, employeeId);
		}

		if (status != null) {
			return leaveRequestRepository.findByStatus(status);
		}

		if (employeeId != null) {
			return leaveRequestRepository.findByEmployeeId(employeeId);
		}

		return leaveRequestRepository.findAll();
	}

	public LeaveResponse createLeave(LeaveCreateRequest request, long actorId) {
		if (request == null) {
			throw new WorkflowException("Leave request is required.");
		}

		AppUser actor = actor(actorId);
		Employee employee = employeeService.getEmployee(request.employeeId());
		if (!hasPeopleAccess(actor) && !employee.getId().equals(requireEmployee(actor).getId())) {
			throw new WorkflowException("You can apply leave only for yourself.");
		}

		LeaveRequest leaveRequest = new LeaveRequest();
		leaveRequest.setEmployee(employee);
		leaveRequest.setLeaveType(requireValue(request.leaveType(), "Leave type is required."));
		leaveRequest.setFromDate(request.fromDate());
		leaveRequest.setToDate(request.toDate());
		leaveRequest.setDescription(clean(request.description()));
		leaveRequest.setAppliedDate(LocalDate.now());
		leaveRequest.setStatus(LeaveStatus.PENDING);

		return LeaveResponse.from(leaveRequestRepository.save(leaveRequest));
	}

	public LeaveDecisionResponse updateDecision(Long id, LeaveDecisionRequest request, long actorId) {
		if (request == null || request.status() == null) {
			throw new WorkflowException("Leave status is required.");
		}

		AppUser actor = actor(actorId);
		LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
				.orElseThrow(() -> new LeaveNotFoundException(id));
		ensureCanDecideLeave(actor, leaveRequest, request.status());

		leaveRequest.setStatus(request.status());
		leaveRequest.setDecisionReason(clean(request.reason()));
		leaveRequest.setDecidedAt(Instant.now());

		LeaveRequest savedLeaveRequest = leaveRequestRepository.saveAndFlush(leaveRequest);
		boolean emailSent = sendNotificationSafely(savedLeaveRequest);
		String message = emailSent ? EMAIL_SENT_MESSAGE : EMAIL_FAILED_MESSAGE;
		notificationService.notifyEmployee(
				savedLeaveRequest.getEmployee().getId(),
				"Leave %s".formatted(savedLeaveRequest.getStatus()),
				"Your leave request was updated to %s.".formatted(savedLeaveRequest.getStatus()),
				NotificationType.LEAVE_UPDATED);

		return new LeaveDecisionResponse(LeaveResponse.from(savedLeaveRequest), true, emailSent, message);
	}

	public void deleteLeave(Long id, long actorId) {
		AppUser actor = actor(actorId);
		LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
				.orElseThrow(() -> new LeaveNotFoundException(id));
		if (!hasPeopleAccess(actor) && !leaveRequest.getEmployee().getId().equals(requireEmployee(actor).getId())) {
			throw new WorkflowException("You can delete only your own leave requests.");
		}

		leaveRequestRepository.delete(leaveRequest);
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String requireValue(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new WorkflowException(message);
		}
		return value.trim();
	}

	private List<LeaveResponse> toResponses(List<LeaveRequest> leaveRequests) {
		return leaveRequests.stream()
				.map(LeaveResponse::from)
				.toList();
	}

	private boolean sendNotificationSafely(LeaveRequest leaveRequest) {
		try {
			return leaveNotificationEmailService.sendDecisionEmail(leaveRequest);
		} catch (Exception exception) {
			logger.error("Leave status was updated, but notification email failed for leave {}.", leaveRequest.getId(), exception);
			return false;
		}
	}

	private AppUser actor(long actorId) {
		return appUserRepository.findById(actorId)
				.orElseThrow(() -> new WorkflowException("Authenticated user was not found."));
	}

	private Employee requireEmployee(AppUser user) {
		if (user.getEmployee() == null) {
			throw new WorkflowException("Your login account is not linked to an employee profile.");
		}
		return user.getEmployee();
	}

	private boolean hasPeopleAccess(AppUser user) {
		return user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.FOUNDER || user.getRole() == UserRole.HR;
	}

	private void ensureCanDecideLeave(AppUser actor, LeaveRequest leaveRequest, LeaveStatus nextStatus) {
		if (hasPeopleAccess(actor)) {
			return;
		}

		Employee actorEmployee = requireEmployee(actor);
		if (actor.getRole() == UserRole.MANAGER && sameDepartment(actorEmployee, leaveRequest.getEmployee())) {
			return;
		}

		if (leaveRequest.getEmployee().getId().equals(actorEmployee.getId()) && nextStatus == LeaveStatus.CANCELLED) {
			return;
		}

		throw new WorkflowException("You do not have permission to update this leave request.");
	}

	private boolean sameDepartment(Employee left, Employee right) {
		return left.getDepartment() != null && right.getDepartment() != null
				&& left.getDepartment().equalsIgnoreCase(right.getDepartment());
	}
}
