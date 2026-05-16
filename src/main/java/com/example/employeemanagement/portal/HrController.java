package com.example.employeemanagement.portal;

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
@RequestMapping("/api/hr")
public class HrController {

	private final CandidateService candidateService;
	private final EmployeeDocumentService employeeDocumentService;
	private final AttendanceService attendanceService;
	private final LearningResourceService learningResourceService;

	public HrController(
			CandidateService candidateService,
			EmployeeDocumentService employeeDocumentService,
			AttendanceService attendanceService,
			LearningResourceService learningResourceService) {
		this.candidateService = candidateService;
		this.employeeDocumentService = employeeDocumentService;
		this.attendanceService = attendanceService;
		this.learningResourceService = learningResourceService;
	}

	@GetMapping("/candidates")
	public List<Candidate> getCandidates() {
		return candidateService.getCandidates();
	}

	@PostMapping("/candidates")
	@ResponseStatus(HttpStatus.CREATED)
	public Candidate createCandidate(@RequestBody Candidate candidate) {
		return candidateService.createCandidate(candidate);
	}

	@PutMapping("/candidates/{id}")
	public Candidate updateCandidate(@PathVariable("id") Long id, @RequestBody Candidate candidate) {
		return candidateService.updateCandidate(id, candidate);
	}

	@DeleteMapping("/candidates/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCandidate(@PathVariable("id") Long id) {
		candidateService.deleteCandidate(id);
	}

	@GetMapping("/documents")
	public List<EmployeeDocument> getDocuments(@RequestParam(name = "employeeId", required = false) Long employeeId) {
		return employeeDocumentService.getDocuments(employeeId);
	}

	@PostMapping("/documents")
	@ResponseStatus(HttpStatus.CREATED)
	public EmployeeDocument createDocument(@RequestBody EmployeeDocument document) {
		return employeeDocumentService.createDocument(document);
	}

	@PutMapping("/documents/{id}")
	public EmployeeDocument updateDocument(@PathVariable("id") Long id, @RequestBody EmployeeDocument document) {
		return employeeDocumentService.updateDocument(id, document);
	}

	@DeleteMapping("/documents/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteDocument(@PathVariable("id") Long id) {
		employeeDocumentService.deleteDocument(id);
	}

	@GetMapping("/attendance")
	public List<Attendance> getAttendance() {
		return attendanceService.getAllAttendance();
	}

	@PostMapping("/attendance")
	@ResponseStatus(HttpStatus.CREATED)
	public Attendance createAttendance(@RequestBody Attendance attendance) {
		return attendanceService.createAttendance(attendance);
	}

	@PutMapping("/attendance/{id}")
	public Attendance updateAttendance(@PathVariable("id") Long id, @RequestBody Attendance attendance) {
		return attendanceService.updateAttendance(id, attendance);
	}

	@DeleteMapping("/attendance/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAttendance(@PathVariable("id") Long id) {
		attendanceService.deleteAttendance(id);
	}

	@GetMapping("/learning-resources")
	public List<LearningResource> getLearningResources() {
		return learningResourceService.getResources();
	}

	@PostMapping("/learning-resources")
	@ResponseStatus(HttpStatus.CREATED)
	public LearningResource createLearningResource(@RequestBody LearningResource resource) {
		return learningResourceService.createResource(resource);
	}

	@PutMapping("/learning-resources/{id}")
	public LearningResource updateLearningResource(@PathVariable("id") Long id, @RequestBody LearningResource resource) {
		return learningResourceService.updateResource(id, resource);
	}

	@DeleteMapping("/learning-resources/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteLearningResource(@PathVariable("id") Long id) {
		learningResourceService.deleteResource(id);
	}
}
