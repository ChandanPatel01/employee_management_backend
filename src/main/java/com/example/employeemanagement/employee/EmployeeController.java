package com.example.employeemanagement.employee;

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
@RequestMapping("/api/employees")
public class EmployeeController {

	private final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@GetMapping
	public List<EmployeeResponse> getEmployees(
			@RequestParam(name = "department", required = false) String department,
			HttpServletRequest servletRequest) {
		return employeeService.getEmployees(department, authenticatedUserId(servletRequest)).stream()
				.map(EmployeeResponse::from)
				.toList();
	}

	@GetMapping("/{id}")
	public EmployeeResponse getEmployee(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
		return EmployeeResponse.from(employeeService.getEmployee(id, authenticatedUserId(servletRequest)));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EmployeeResponse createEmployee(@Valid @RequestBody Employee employee, HttpServletRequest servletRequest) {
		return EmployeeResponse.from(employeeService.createEmployee(employee, authenticatedUserId(servletRequest)));
	}

	@PutMapping("/{id}")
	public EmployeeResponse updateEmployee(@PathVariable("id") Long id, @Valid @RequestBody Employee employee, HttpServletRequest servletRequest) {
		return EmployeeResponse.from(employeeService.updateEmployee(id, employee, authenticatedUserId(servletRequest)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteEmployee(@PathVariable("id") Long id, HttpServletRequest servletRequest) {
		employeeService.deleteEmployee(id, authenticatedUserId(servletRequest));
	}

	private long authenticatedUserId(HttpServletRequest request) {
		Object userId = request.getAttribute("authenticatedUserId");
		if (userId instanceof Number number) {
			return number.longValue();
		}

		throw new InvalidCredentialsException();
	}
}
