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
				ensureUserRoleColumn(connection);
				migrateBase64Photos(connection);
				if (!columnExists(connection, "employees", "employee_code")) {
					jdbcTemplate.execute("ALTER TABLE employees ADD COLUMN employee_code VARCHAR(32)");
				}
				jdbcTemplate.execute("""
						UPDATE employees
						SET employee_code = CONCAT(UPPER(SUBSTRING(first_name, 1, 1)), UPPER(SUBSTRING(last_name, 1, 1)), LPAD(id, 4, '0'))
						WHERE employee_code IS NULL OR employee_code = ''
						""");
			}
		}
	}

	private void ensureUserRoleColumn(Connection connection) throws Exception {
		if (!columnExists(connection, "app_users", "role")) {
			jdbcTemplate.execute("ALTER TABLE app_users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER'");
		}

		jdbcTemplate.execute("UPDATE app_users SET role = 'USER' WHERE role IS NULL OR role = ''");
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
}
