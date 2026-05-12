package com.example.employeemanagement.timeoff;

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
	public List<LeaveRequest> getLeaves(
			@RequestParam(name = "status", required = false) LeaveStatus status,
			@RequestParam(name = "employeeId", required = false) Long employeeId) {
		return leaveRequestService.getLeaves(status, employeeId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LeaveRequest createLeave(@Valid @RequestBody LeaveCreateRequest request) {
		return leaveRequestService.createLeave(request);
	}

	@PutMapping("/{id}/decision")
	public LeaveRequest updateDecision(@PathVariable("id") Long id, @Valid @RequestBody LeaveDecisionRequest request) {
		return leaveRequestService.updateDecision(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteLeave(@PathVariable("id") Long id) {
		leaveRequestService.deleteLeave(id);
	}
}
