package com.sametkorkmaz.walletledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LedgerSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.batchUpdate("DELETE FROM journal_lines;",
                "DELETE FROM journal_entries;",
                "DELETE FROM accounts;",
                "DELETE FROM users;");
    }

    @Test
    void secondExternalAccountInSameCurrencyIsRejected() {
        jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (NULL,'EUR')");
        assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (NULL,'EUR')"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_accounts_external_currency");
    }
}
