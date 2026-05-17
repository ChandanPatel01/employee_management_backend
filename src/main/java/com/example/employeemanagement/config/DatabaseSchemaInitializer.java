package com.example.employeemanagement.config;

import com.example.employeemanagement.upload.EmployeePhotoStorageService;
import com.example.employeemanagement.upload.UploadException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseSchemaInitializer implements ApplicationRunner {

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	private final EmployeePhotoStorageService employeePhotoStorageService;

	public DatabaseSchemaInitializer(
			DataSource dataSource,
			JdbcTemplate jdbcTemplate,
			EmployeePhotoStorageService employeePhotoStorageService) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
		this.employeePhotoStorageService = employeePhotoStorageService;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			String database = connection.getMetaData().getDatabaseProductName().toLowerCase();
			if (database.contains("mysql") || database.contains("mariadb")) {
				ensureUserNotificationsTable(connection);
				ensureEmployeeProfileColumns(connection);
				ensureUserRoleColumn(connection);
				ensureUserEmployeeColumn(connection);
				backfillUserEmployeeLinks();
				migrateBase64Photos(connection);
			}
		}
	}

	private void ensureUserNotificationsTable(Connection connection) throws Exception {
		if (!tableExists(connection, "user_notifications")) {
			jdbcTemplate.execute("""
					CREATE TABLE user_notifications (
						id BIGINT NOT NULL AUTO_INCREMENT,
						user_id BIGINT NOT NULL,
						title VARCHAR(255) NOT NULL,
						message VARCHAR(2000) NOT NULL,
						type VARCHAR(40) NOT NULL DEFAULT 'GENERAL',
						`read` TINYINT(1) NOT NULL DEFAULT 0,
						created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
						PRIMARY KEY (id),
						INDEX idx_user_notifications_user_created_at (user_id, created_at)
					)
					""");
			return;
		}

		if (!columnExists(connection, "user_notifications", "user_id")) {
			jdbcTemplate.execute("ALTER TABLE user_notifications ADD COLUMN user_id BIGINT NULL");
		}
		if (!columnExists(connection, "user_notifications", "title")) {
			jdbcTemplate.execute("ALTER TABLE user_notifications ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT 'Notification'");
		}
		if (!columnExists(connection, "user_notifications", "message")) {
			jdbcTemplate.execute("ALTER TABLE user_notifications ADD COLUMN message VARCHAR(2000) NOT NULL DEFAULT ''");
		}
		if (!columnExists(connection, "user_notifications", "type")) {
			jdbcTemplate.execute("ALTER TABLE user_notifications ADD COLUMN type VARCHAR(40) NOT NULL DEFAULT 'GENERAL'");
		}
		if (!columnExists(connection, "user_notifications", "read")) {
			jdbcTemplate.execute("ALTER TABLE user_notifications ADD COLUMN `read` TINYINT(1) NOT NULL DEFAULT 0");
		}
		if (!columnExists(connection, "user_notifications", "created_at")) {
			jdbcTemplate.execute("ALTER TABLE user_notifications ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)");
		}
		if (!indexExists(connection, "user_notifications", "idx_user_notifications_user_created_at")) {
			jdbcTemplate.execute("CREATE INDEX idx_user_notifications_user_created_at ON user_notifications (user_id, created_at)");
		}
	}

	private void ensureEmployeeProfileColumns(Connection connection) throws Exception {
		if (!columnExists(connection, "employees", "employee_code")) {
			jdbcTemplate.execute("ALTER TABLE employees ADD COLUMN employee_code VARCHAR(32)");
		}
		if (!columnExists(connection, "employees", "phone")) {
			jdbcTemplate.execute("ALTER TABLE employees ADD COLUMN phone VARCHAR(255)");
		}

		jdbcTemplate.execute("""
				UPDATE employees
				SET employee_code = CONCAT(UPPER(SUBSTRING(first_name, 1, 1)), UPPER(SUBSTRING(last_name, 1, 1)), LPAD(id, 4, '0'))
				WHERE employee_code IS NULL OR employee_code = ''
				""");
	}

	private void ensureUserRoleColumn(Connection connection) throws Exception {
		if (!columnExists(connection, "app_users", "role")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE'");
		}

		jdbcTemplate.execute("UPDATE app_users SET role = 'EMPLOYEE' WHERE role IS NULL OR role = '' OR role = 'USER' OR role NOT IN ('EMPLOYEE', 'INTERN', 'MANAGER', 'HR', 'ADMIN', 'FOUNDER')");
		jdbcTemplate.execute("ALTER TABLE app_users MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'EMPLOYEE'");

		if (!columnExists(connection, "app_users", "force_password_change")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN force_password_change TINYINT(1) NOT NULL DEFAULT 0");
		}
		if (!columnExists(connection, "app_users", "blocked")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN blocked TINYINT(1) NOT NULL DEFAULT 0");
		}
		if (!columnExists(connection, "app_users", "password_changed")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN password_changed TINYINT(1) NOT NULL DEFAULT 1");
		}
		if (!columnExists(connection, "app_users", "password_changed_at")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN password_changed_at TIMESTAMP NULL");
		}
		if (!columnExists(connection, "app_users", "created_by")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN created_by VARCHAR(255)");
		}
	}

	private void ensureUserEmployeeColumn(Connection connection) throws Exception {
		if (!columnExists(connection, "app_users", "employee_id")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN employee_id BIGINT NULL");
		}

		if (!indexExists(connection, "app_users", "uk_app_users_employee_id")) {
			jdbcTemplate.execute("CREATE UNIQUE INDEX uk_app_users_employee_id ON app_users (employee_id)");
		}
	}

	private void backfillUserEmployeeLinks() {
		jdbcTemplate.execute("""
				UPDATE app_users u
				JOIN employees e ON LOWER(e.email) = LOWER(u.email)
				SET u.employee_id = e.id
				WHERE u.employee_id IS NULL
				""");

		jdbcTemplate.execute("""
				INSERT INTO employees (
					employee_code,
					first_name,
					last_name,
					email,
					department,
					job_title,
					salary,
					hire_date,
					status
				)
				SELECT
					CONCAT('U', LPAD(u.id, 5, '0')),
					COALESCE(NULLIF(SUBSTRING_INDEX(TRIM(COALESCE(u.name, '')), ' ', 1), ''), CASE u.role
						WHEN 'FOUNDER' THEN 'Founder'
						WHEN 'ADMIN' THEN 'Administrator'
						WHEN 'HR' THEN 'HR Executive'
						WHEN 'MANAGER' THEN 'Manager'
						WHEN 'INTERN' THEN 'Intern'
						ELSE 'Employee'
					END),
					COALESCE(NULLIF(
						CASE
							WHEN TRIM(COALESCE(u.name, '')) LIKE '% %'
								THEN TRIM(SUBSTRING(TRIM(u.name), LOCATE(' ', TRIM(u.name)) + 1))
							ELSE 'User'
						END,
						''
					), 'User'),
					u.email,
					CASE u.role
						WHEN 'FOUNDER' THEN 'Leadership'
						WHEN 'ADMIN' THEN 'Administration'
						WHEN 'HR' THEN 'Human Resources'
						WHEN 'MANAGER' THEN 'Management'
						WHEN 'INTERN' THEN 'Internship'
						ELSE 'Operations'
					END,
					CASE u.role
						WHEN 'FOUNDER' THEN 'Founder'
						WHEN 'ADMIN' THEN 'Administrator'
						WHEN 'HR' THEN 'HR Executive'
						WHEN 'MANAGER' THEN 'Manager'
						WHEN 'INTERN' THEN 'Intern'
						ELSE 'Employee'
					END,
					0,
					CURRENT_DATE,
					'ACTIVE'
				FROM app_users u
				LEFT JOIN employees e ON LOWER(e.email) = LOWER(u.email)
				WHERE u.employee_id IS NULL
					AND e.id IS NULL
				""");

		jdbcTemplate.execute("""
				UPDATE app_users u
				JOIN employees e ON LOWER(e.email) = LOWER(u.email)
				SET u.employee_id = e.id
				WHERE u.employee_id IS NULL
				""");
	}

	private void migrateBase64Photos(Connection connection) throws Exception {
		if (!columnExists(connection, "employees", "photo_url")) {
			return;
		}

		List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
				SELECT id, photo_url
				FROM employees
				WHERE photo_url LIKE 'data:image/%'
				""");

		for (Map<String, Object> row : rows) {
			Long id = ((Number) row.get("id")).longValue();
			String dataUrl = (String) row.get("photo_url");
			try {
				String photoUrl = employeePhotoStorageService.storeDataUrl(id, dataUrl);
				jdbcTemplate.update("UPDATE employees SET photo_url = ? WHERE id = ?", photoUrl, id);
			} catch (UploadException exception) {
				jdbcTemplate.update("UPDATE employees SET photo_url = NULL WHERE id = ?", id);
			}
		}

		jdbcTemplate.execute("UPDATE employees SET photo_url = NULL WHERE photo_url IS NOT NULL AND CHAR_LENGTH(photo_url) > 1024");
		jdbcTemplate.execute("ALTER TABLE employees MODIFY COLUMN photo_url VARCHAR(1024)");
	}

	private boolean columnExists(Connection connection, String tableName, String columnName) throws Exception {
		try (ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
			return columns.next();
		}
	}

	private boolean tableExists(Connection connection, String tableName) throws Exception {
		try (ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, new String[] {"TABLE"})) {
			return tables.next();
		}
	}

	private boolean indexExists(Connection connection, String tableName, String indexName) throws Exception {
		try (ResultSet indexes = connection.getMetaData().getIndexInfo(null, null, tableName, false, false)) {
			while (indexes.next()) {
				if (indexName.equalsIgnoreCase(indexes.getString("INDEX_NAME"))) {
					return true;
				}
			}

			return false;
		}
	}
}
