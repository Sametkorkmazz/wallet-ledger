package com.sametkorkmaz.walletledger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

//    @Test
//    void zeroAmountIsRejected() {
//
//    }

    @Nested
    @DisplayName("accounts")
    class Accounts {

        @Test
        void accountForNonExistentUserIsRejected() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", UUID.randomUUID()))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_accounts_user");
        }

        @ParameterizedTest
        @ValueSource(strings = {"EUR", "USD", "TRY"})
        void validCurrencyAccepted(String currency) {
            assertDoesNotThrow(() -> jdbcTemplate.update("INSERT INTO accounts (currency) VALUES (?)", currency));
            int count = jdbcTemplate.queryForObject("SELECT count(*) FROM accounts", Integer.class);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void invalidCurrencyRejected() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (currency) VALUES ('XYZ')"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_accounts_currency");
        }

        @Test
        void sameAccountInSameCurrencyIsRejected() {
            UUID userId = jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash) VALUES (?, ?) RETURNING id",
                    UUID.class, "a@test.com", "hash");
            jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", userId);
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", userId))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_accounts_user_currency");
        }

        @Test
        void secondExternalAccountInSameCurrencyIsRejected() {
            jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (NULL,'EUR')");
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (NULL,'EUR')"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_accounts_external_currency");
        }

    }
}
