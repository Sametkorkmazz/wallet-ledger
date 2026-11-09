package com.sametkorkmaz.walletledger.ledger.persistence;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

public final class TestData {
    private TestData() {
    }

    public static UUID insertUser(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash) values (?,'test123') RETURNING ID",
                UUID.class, UUID.randomUUID() + "@test.com");
    }

    /**
     * Hardcodes currency EUR
     */
    public static UUID insertAccount(JdbcTemplate jdbcTemplate) {
        UUID userId = insertUser(jdbcTemplate);
        return jdbcTemplate.queryForObject("INSERT INTO accounts (user_id, currency) VALUES  (?,'EUR') RETURNING id", UUID.class, userId);
    }

    public static UUID insertEntry(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForObject("INSERT INTO journal_entries (description) VALUES  ('test') RETURNING id", UUID.class);
    }
}
