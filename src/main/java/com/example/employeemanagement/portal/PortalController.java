package com.example.employeemanagement.portal;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.employee.Employee;
import com.example.employeemanagement.employee.EmployeeRepository;
import com.example.employeemanagement.timeoff.LeaveCreateRequest;
import com.example.employeemanagement.timeoff.LeaveRequest;
import com.example.employeemanagement.timeoff.LeaveRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

	private final AppUserRepository appUserRepository;
	private final EmployeeRepository employeeRepository;
	private final TaskService taskService;
	private final DailyUpdateService dailyUpdateService;
	private final LearningResourceService learningResourceService;
	private final AttendanceService attendanceService;
	private final LeaveRequestService leaveRequestService;

	public PortalController(
			AppUserRepository appUserRepository,
			EmployeeRepository employeeRepository,
			TaskService taskService,
			DailyUpdateService dailyUpdateService,
			LearningResourceService learningResourceService,
			AttendanceService attendanceService,
			LeaveRequestService leaveRequestService) {
		this.appUserRepository = appUserRepository;
		this.employeeRepository = employeeRepository;
		this.taskService = taskService;
		this.dailyUpdateService = dailyUpdateService;
		this.learningResourceService = learningResourceService;
		this.attendanceService = attendanceService;
		this.leaveRequestService = leaveRequestService;
	}

	@GetMapping("/profile")
	public ProfileResponse getProfile(HttpServletRequest request) {
		String email = currentEmail(request);
		AppUser user = appUserRepository.findByEmail(email)
				.orElseThrow(() -> new PortalResourceNotFoundException("Profile not found."));
		Employee employee = employeeRepository.findByEmail(email).orElse(null);
		return ProfileResponse.from(user, employee);
	}

	@GetMapping("/my-tasks")
	public List<TaskItem> getMyTasks(HttpServletRequest request) {
		return taskService.getTasksForUser(currentEmail(request));
	}

	@GetMapping("/daily-updates")
	public List<DailyUpdate> getMyDailyUpdates(HttpServletRequest request) {
		return dailyUpdateService.getUpdatesForUser(currentEmail(request));
	}

	@PostMapping("/daily-updates")
	@ResponseStatus(HttpStatus.CREATED)
	public DailyUpdate createDailyUpdate(HttpServletRequest request, @RequestBody DailyUpdate dailyUpdate) {
		return dailyUpdateService.createUpdate(currentEmail(request), dailyUpdate);
	}

	@PutMapping("/daily-updates/{id}")
	public DailyUpdate updateDailyUpdate(
			HttpServletRequest request,
			@PathVariable("id") Long id,
			@RequestBody DailyUpdate dailyUpdate) {
		return dailyUpdateService.updateUpdate(id, currentEmail(request), dailyUpdate);
	}

	@DeleteMapping("/daily-updates/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDailyUpdate(@PathVariable("id") Long id) {
		dailyUpdateService.deleteUpdate(id);
	}

	@GetMapping("/learning-resources")
	public List<LearningResource> getLearningResources() {
		return learningResourceService.getResources();
	}

	@GetMapping("/my-attendance")
	public List<Attendance> getMyAttendance(HttpServletRequest request) {
		return attendanceService.getAttendanceForUser(currentEmail(request));
	}

	@GetMapping("/my-leaves")
	public List<LeaveRequest> getMyLeaves(HttpServletRequest request) {
		return employeeRepository.findByEmail(currentEmail(request))
				.map(employee -> leaveRequestService.getLeaves(null, employee.getId()))
				.orElseGet(List::of);
	}

	@PostMapping("/my-leaves")
	@ResponseStatus(HttpStatus.CREATED)
	public LeaveRequest createMyLeave(HttpServletRequest request, @Valid @RequestBody MyLeaveRequest myLeaveRequest) {
		Employee employee = employeeRepository.findByEmail(currentEmail(request))
				.orElseThrow(() -> new PortalResourceNotFoundException("Employee profile not found for this user."));
		LeaveCreateRequest createRequest = new LeaveCreateRequest(
				employee.getId(),
				myLeaveRequest.leaveType(),
				myLeaveRequest.fromDate(),
				myLeaveRequest.toDate(),
				myLeaveRequest.description());
		return leaveRequestService.createLeave(createRequest);
	}

	private String currentEmail(HttpServletRequest request) {
		Object email = request.getAttribute("authenticatedUserEmail");
		return email == null ? "" : email.toString();
	}
}
