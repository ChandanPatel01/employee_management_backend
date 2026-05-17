package com.example.employeemanagement.audit;

import com.example.employeemanagement.auth.AppUser;
import com.example.employeemanagement.employee.Employee;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	public void recordEmployeeDeactivated(AppUser actor, Employee employee, boolean linkedUserBlocked) {
		AuditLog auditLog = new AuditLog();
		auditLog.setAction("EMPLOYEE_DEACTIVATED");
		auditLog.setActorUserId(actor == null ? null : actor.getId());
		auditLog.setActorEmail(actor == null ? null : actor.getEmail());
		auditLog.setActorRole(actor == null ? null : actor.getRole());
		auditLog.setTargetEmployeeId(employee == null ? null : employee.getId());
		auditLog.setTargetEmployeeCode(employee == null ? null : employee.getEmployeeCode());
		auditLog.setTargetEmployeeEmail(employee == null ? null : employee.getEmail());
		auditLog.setTargetEmployeeName(fullName(employee));
		auditLog.setDetails("Employee was marked INACTIVE. Linked user blocked: %s.".formatted(linkedUserBlocked));
		auditLogRepository.save(auditLog);
	}

	private String fullName(Employee employee) {
		if (employee == null) {
			return null;
		}
		return ("%s %s".formatted(
				employee.getFirstName() == null ? "" : employee.getFirstName(),
				employee.getLastName() == null ? "" : employee.getLastName())).trim();
	}
}
