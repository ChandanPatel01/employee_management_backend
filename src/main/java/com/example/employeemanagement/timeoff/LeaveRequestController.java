package com.example.employeemanagement.timeoff;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveRequestController {

	private final LeaveRequestService leaveRequestService;

	public LeaveRequestController(LeaveRequestService leaveRequestService) {
		this.leaveRequestService = leaveRequestService;
	}

	@GetMapping
	public List<LeaveResponse> getLeaves(
			@RequestParam(name = "status", required = false) LeaveStatus status,
			@RequestParam(name = "employeeId", required = false) Long employeeId,
			HttpServletRequest servletRequest) {
		return leaveRequestService.getLeaves(status, employeeId, authenticatedUserId(servletRequest));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LeaveResponse createLeave(@Valid @RequestBody LeaveCreateRequest request, HttpServletRequest servletRequest) {
		return leaveRequestService.createLeave(request, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/decision")
	public LeaveDecisionResponse updateDecision(
			@PathVariable("id") Long id,
			@Valid @RequestBody LeaveDecisionRequest request,
			HttpServletRequest servletRequest) {
		return leaveRequestService.updateDecision(id, request, authenticatedUserId(servletRequest));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteLeave(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
		leaveRequestService.deleteLeave(id, authenticatedUserId(servletRequest));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}

		throw new InvalidCredentialsException();
	}
}
