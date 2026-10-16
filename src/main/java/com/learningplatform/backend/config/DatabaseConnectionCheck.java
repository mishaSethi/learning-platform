package com.learningplatform.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup and logs which PostgreSQL server we are connected to.
 * Purely diagnostic - makes a successful (or failed) Neon connection obvious
 * in the logs without exposing any credentials.
 */
@Component
@Profile("!test") // tests run without a database
public class DatabaseConnectionCheck implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionCheck.class);

	private final JdbcTemplate jdbcTemplate;

	public DatabaseConnectionCheck(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		String version = jdbcTemplate.queryForObject("select version()", String.class);
		String database = jdbcTemplate.queryForObject("select current_database()", String.class);
		log.info("PostgreSQL connection OK - database '{}' on {}", database, version);
	}
}
