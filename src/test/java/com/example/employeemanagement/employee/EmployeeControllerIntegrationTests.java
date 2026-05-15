package com.example.employeemanagement.employee;

import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.AuthRequest;
import com.example.employeemanagement.auth.SignupRequest;
import com.example.employeemanagement.auth.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

	@Autowired
	private AppUserRepository appUserRepository;

	@BeforeEach
	void setUp() {
		employeeRepository.deleteAll();
		appUserRepository.deleteAll();
	}

	@Test
	void createEmployeeReturnsCreatedEmployee() throws Exception {
		String authorization = authorizationHeader();

		mockMvc.perform(post("/api/employees")
						.header(HttpHeaders.AUTHORIZATION, authorization)
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
		String authorization = authorizationHeader();
		employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));
		employeeRepository.save(employee("Grace", "Hopper", "grace@example.com", "Engineering"));
		employeeRepository.save(employee("Mary", "Jackson", "mary@example.com", "Finance"));

		mockMvc.perform(get("/api/employees")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.param("department", "engineering"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].department").value("Engineering"))
				.andExpect(jsonPath("$[1].department").value("Engineering"));
	}

	@Test
	void getEmployeeByIdReturnsEmployee() throws Exception {
		String authorization = authorizationHeader();
		Employee savedEmployee = employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));

		mockMvc.perform(get("/api/employees/{id}", savedEmployee.getId())
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(savedEmployee.getId()))
				.andExpect(jsonPath("$.email").value("ada@example.com"));
	}

	@Test
	void updateEmployeeReturnsUpdatedEmployee() throws Exception {
		String authorization = authorizationHeader();
		Employee savedEmployee = employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));
		Employee updatedEmployee = employee("Ada", "Byron", "ada.byron@example.com", "Research");
		updatedEmployee.setJobTitle("Principal Engineer");
		updatedEmployee.setSalary(new BigDecimal("98000.00"));
		updatedEmployee.setStatus(EmploymentStatus.ON_LEAVE);

		mockMvc.perform(put("/api/employees/{id}", savedEmployee.getId())
						.header(HttpHeaders.AUTHORIZATION, authorization)
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
		String authorization = authorizationHeader();
		Employee savedEmployee = employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));

		mockMvc.perform(delete("/api/employees/{id}", savedEmployee.getId())
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/employees/{id}", savedEmployee.getId())
						.header(HttpHeaders.AUTHORIZATION, authorization))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Employee with id " + savedEmployee.getId() + " was not found"));
	}

	@Test
	void createEmployeeWithInvalidPayloadReturnsValidationErrors() throws Exception {
		String authorization = authorizationHeader();
		Employee invalidEmployee = employee("", "", "not-an-email", "");
		invalidEmployee.setSalary(new BigDecimal("-1.00"));
		invalidEmployee.setHireDate(null);

		mockMvc.perform(post("/api/employees")
						.header(HttpHeaders.AUTHORIZATION, authorization)
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
		String authorization = authorizationHeader();
		employeeRepository.save(employee("Ada", "Lovelace", "ada@example.com", "Engineering"));

		mockMvc.perform(post("/api/employees")
						.header(HttpHeaders.AUTHORIZATION, authorization)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(employee("Grace", "Hopper", "ada@example.com", "Engineering"))))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Employee email already exists: ada@example.com"));
	}

	@Test
	void employeeEndpointsRequireJwt() throws Exception {
		mockMvc.perform(get("/api/employees"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Missing bearer token"));
	}

	@Test
	void signupAndLoginReturnJwt() throws Exception {
		SignupRequest signupRequest = new SignupRequest("Test Admin", "admin@example.com", "password123");

		mockMvc.perform(post("/api/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signupRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("admin@example.com"));

		AuthRequest loginRequest = new AuthRequest("admin@example.com", "password123");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.user.name").value("Test Admin"));
	}

	@Test
	void corsPreflightAllowsLocalFrontend() throws Exception {
		String frontendOrigin = "http://localhost:5173";

		mockMvc.perform(options("/api/auth/signup")
						.header(HttpHeaders.ORIGIN, frontendOrigin)
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, frontendOrigin))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("content-type")));
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

	private String authorizationHeader() throws Exception {
		String email = "tester" + System.nanoTime() + "@example.com";
		SignupRequest signupRequest = new SignupRequest("Test Admin", email, "password123");

		mockMvc.perform(post("/api/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signupRequest)))
				.andExpect(status().isCreated());

		appUserRepository.findByEmail(email).ifPresent((user) -> {
			user.setRole(UserRole.ADMIN);
			appUserRepository.save(user);
		});

		AuthRequest loginRequest = new AuthRequest(email, "password123");
		String response = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginRequest)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String token = objectMapper.readTree(response).get("token").asText();
		return "Bearer " + token;
	}
}
