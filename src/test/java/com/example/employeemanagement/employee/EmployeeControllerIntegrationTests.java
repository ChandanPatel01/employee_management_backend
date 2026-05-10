package com.example.employeemanagement.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EmployeeRepository employeeRepository;

	@BeforeEach
	void setUp() {
		employeeRepository.deleteAll();
	}

	@Test
	void createEmployeeReturnsCreatedEmployee() throws Exception {
		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(employee("Ada", "Lovelace", "ada@example.com", "Engineering"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.firstName").value("Ada"))
				.andExpect(jsonPath("$.lastName").value("Lovelace"))
				.andExpect(jsonPath("$.email").value("ada@example.com"))
				.andExpect(jsonPath("$.department").value("Engineering"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void getEmployeesCanFilterByDepartmentIgnoringCase() throws Exception {
		employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));
		employeeRepository.save(employee("Grace", "Hopper", "grace@example.com", "Engineering"));
		employeeRepository.save(employee("Mary", "Jackson", "mary@example.com", "Finance"));

		mockMvc.perform(get("/api/employees").param("department", "engineering"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].department").value("Engineering"))
				.andExpect(jsonPath("$[1].department").value("Engineering"));
	}

	@Test
	void getEmployeeByIdReturnsEmployee() throws Exception {
		Employee savedEmployee = employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));

		mockMvc.perform(get("/api/employees/{id}", savedEmployee.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedEmployee.getId()))
				.andExpect(jsonPath("$.email").value("ada@example.com"));
	}

	@Test
	void updateEmployeeReturnsUpdatedEmployee() throws Exception {
		Employee savedEmployee = employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));
		Employee updatedEmployee = employee("Ada", "Byron", "ada.byron@example.com", "Research");
		updatedEmployee.setJobTitle("Principal Engineer");
		updatedEmployee.setSalary(new BigDecimal("98000.00"));
		updatedEmployee.setStatus(EmploymentStatus.ON_LEAVE);

		mockMvc.perform(put("/api/employees/{id}", savedEmployee.getId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedEmployee)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedEmployee.getId()))
				.andExpect(jsonPath("$.lastName").value("Byron"))
				.andExpect(jsonPath("$.email").value("ada.byron@example.com"))
				.andExpect(jsonPath("$.department").value("Research"))
				.andExpect(jsonPath("$.jobTitle").value("Principal Engineer"))
				.andExpect(jsonPath("$.status").value("ON_LEAVE"));
	}

	@Test
	void deleteEmployeeRemovesEmployee() throws Exception {
		Employee savedEmployee = employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));

		mockMvc.perform(delete("/api/employees/{id}", savedEmployee.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/employees/{id}", savedEmployee.getId()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Employee with id " + savedEmployee.getId() + " was not found"));
	}

	@Test
	void createEmployeeWithInvalidPayloadReturnsValidationErrors() throws Exception {
		Employee invalidEmployee = employee("", "", "not-an-email", "");
		invalidEmployee.setSalary(new BigDecimal("-1.00"));
		invalidEmployee.setHireDate(null);

		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(invalidEmployee)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.validationErrors.firstName").exists())
				.andExpect(jsonPath("$.validationErrors.lastName").exists())
				.andExpect(jsonPath("$.validationErrors.email").exists())
				.andExpect(jsonPath("$.validationErrors.department").exists())
				.andExpect(jsonPath("$.validationErrors.salary").exists())
				.andExpect(jsonPath("$.validationErrors.hireDate").exists());
	}

	@Test
	void createEmployeeWithDuplicateEmailReturnsConflict() throws Exception {
		employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));

		mockMvc.perform(post("/api/employees")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(employee("Grace", "Hopper", "ada@example.com", "Engineering"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Employee email already exists: ada@example.com"));
	}

	private Employee employee(String firstName, String lastName, String email, String department) {
		Employee employee = new Employee();
		employee.setFirstName(firstName);
		employee.setLastName(lastName);
		employee.setEmail(email);
		employee.setDepartment(department);
		employee.setJobTitle("Software Engineer");
		employee.setSalary(new BigDecimal("75000.00"));
		employee.setHireDate(LocalDate.of(2024, 1, 15));
		employee.setStatus(EmploymentStatus.ACTIVE);
		return employee;
	}
}
