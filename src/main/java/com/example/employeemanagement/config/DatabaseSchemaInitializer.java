package com.example.employeemanagement.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {

	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;

	public DatabaseSchemaInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
		this.dataSource = dataSource;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			String database = connection.getMetaData().getDatabaseProductName().toLowerCase();
			if (database.contains("mysql") || database.contains("mariadb")) {
				jdbcTemplate.execute("ALTER TABLE employees MODIFY COLUMN photo_url LONGTEXT");
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

	private boolean columnExists(Connection connection, String tableName, String columnName) throws Exception {
		try (ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
			return columns.next();
		}
	}
}
