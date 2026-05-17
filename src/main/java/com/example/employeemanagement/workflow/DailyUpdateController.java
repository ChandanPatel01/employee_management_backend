package com.example.employeemanagement.workflow;

import com.example.employeemanagement.auth.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/daily-updates")
public class DailyUpdateController {

	private final DailyUpdateService dailyUpdateService;

	public DailyUpdateController(DailyUpdateService dailyUpdateService) {
		this.dailyUpdateService = dailyUpdateService;
	}

	@GetMapping("/my")
	public List<DailyUpdateResponse> getMyUpdates(HttpServletRequest request) {
		return dailyUpdateService.getMyUpdates(authenticatedUserId(request));
	}

	@GetMapping("/team")
	public List<DailyUpdateResponse> getTeamUpdates(HttpServletRequest request) {
		return dailyUpdateService.getTeamUpdates(authenticatedUserId(request));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DailyUpdateResponse submitUpdate(@Valid @RequestBody DailyUpdateRequest request, HttpServletRequest servletRequest) {
		return dailyUpdateService.submitUpdate(request, authenticatedUserId(servletRequest));
	}

	@PutMapping("/{id}/review")
	public DailyUpdateResponse reviewUpdate(
			@PathVariable("id") Long id,
			@RequestBody DailyUpdateReviewRequest request,
			HttpServletRequest servletRequest) {
		return dailyUpdateService.reviewUpdate(id, request, authenticatedUserId(servletRequest));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}
		throw new InvalidCredentialsException();
	}
}
