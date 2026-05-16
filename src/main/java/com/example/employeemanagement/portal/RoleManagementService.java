package com.example.employeemanagement.portal;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.auth.AppUserRepository;
import com.example.employeemanagement.auth.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class RoleManagementService {

	private final AppUserRepository appUserRepository;

	public RoleManagementService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Transactional(readOnly = true)
	public List<ManagedUserResponse> getUsers() {
		return appUserRepository.findAll().stream()
				.map(ManagedUserResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<UserRole> getRoles() {
		return Arrays.asList(UserRole.values());
	}

	public ManagedUserResponse updateRole(Long id, UserRole role) {
		AppUser user = appUserRepository.findById(id)
				.orElseThrow(() -> new PortalResourceNotFoundException("User", id));
		user.setRole(role);
		return ManagedUserResponse.from(appUserRepository.save(user));
	}
}
