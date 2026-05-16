package com.example.employeemanagement.portal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {

	private final ProjectRepository projectRepository;

	public ProjectService(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Transactional(readOnly = true)
	public List<Project> getProjects() {
		return projectRepository.findAllByOrderByDeadlineAsc();
	}

	public Project createProject(Project request) {
		Project project = new Project();
		apply(request, project);
		return projectRepository.save(project);
	}

	public Project updateProject(Long id, Project request) {
		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("Project", id));
		apply(request, project);
		return projectRepository.save(project);
	}

	public void deleteProject(Long id) {
		if (!projectRepository.existsById(id)) {
			throw new PortalResourceNotFoundException("Project", id);
		}

		projectRepository.deleteById(id);
	}

	private void apply(Project source, Project target) {
		target.setProjectName(defaulted(source.getProjectName(), "Untitled project"));
		target.setClientName(defaulted(source.getClientName(), "Internal"));
		target.setStartDate(source.getStartDate());
		target.setDeadline(source.getDeadline());
		target.setStatus(defaulted(source.getStatus(), "PLANNED").toUpperCase());
		target.setAssignedTeam(clean(source.getAssignedTeam()));
	}

	private String clean(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String defaulted(String value, String fallback) {
		String cleaned = clean(value);
		return cleaned == null ? fallback : cleaned;
	}
}
