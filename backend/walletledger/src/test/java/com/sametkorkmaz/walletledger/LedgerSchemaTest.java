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
    // mirrors VARCHAR(255) in V2__ledger.sql
    private static final int MAX_DESCRIPTION_LENGTH = 255;
    // mirrors NUMERIC(19, 4) in V2__ledger.sql
    private static final int MAX_SCALE = 4;
    private static final int MAX_INTEGER_DIGITS = 15;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID insertAccount(String currency) {
        return jdbcTemplate.queryForObject("INSERT INTO accounts (currency) VALUES (?) RETURNING id", UUID.class,
                currency);
    }

    private UUID insertEntry(String description) {
        return jdbcTemplate.queryForObject("INSERT INTO journal_entries (description) VALUES (?) RETURNING id", UUID.class,
                description);
    }

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.batchUpdate("DELETE FROM journal_lines;",
                "DELETE FROM journal_entries;",
                "DELETE FROM accounts;",
                "DELETE FROM users;");
    }

    @Nested
    @DisplayName("users")
    class Users {
        @Test
        void userWithDuplicateEmailIsRejected() {
            jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "test@example.com", "test123");
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "test@example.com", "test456"))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void userWithSameEmailInDifferentCaseIsRejected() {
            jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "test@example.com", "test123");
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "TEST@example.com", "test456"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_users_email_lower");
        }
    }

    @Nested
    @DisplayName("accounts")
    class Accounts {

        @Test
        void accountForNonExistentUserIsRejected() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", UUID.randomUUID()))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_accounts_users");
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

    @Nested
    @DisplayName("journal_lines")
    class JournalLines {

        private void insertLedgerLine(UUID accountId, UUID entryId, int amount) {
            jdbcTemplate.update("INSERT INTO journal_lines (account_id, entry_id, amount) VALUES (?,?,?)",
                    accountId, entryId, amount);
        }

        @Test
        void amountZeroRejected() {
            UUID accountId = insertAccount("EUR");
            UUID entryId = insertEntry("TEST");
            assertThatThrownBy(() -> insertLedgerLine(accountId, entryId, 0))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_journal_lines_amount_not_zero");
        }

        @Test
        void amountColumnMatchesDomainLimits() {
            int columnScale = jdbcTemplate.queryForObject("""
                    SELECT numeric_scale FROM information_schema.columns
                    WHERE table_schema = 'public'
                    AND table_name = 'journal_lines'
                    AND column_name = 'amount'
                    """, Integer.class);
            int columnIntegerDigits = jdbcTemplate.queryForObject("""
                    SELECT numeric_precision - numeric_scale FROM information_schema.columns
                    WHERE table_schema = 'public'
                    AND table_name = 'journal_lines'
                    AND column_name = 'amount'
                    """, Integer.class);
            assertThat(columnScale).isEqualTo(MAX_SCALE);
            assertThat(columnIntegerDigits).isEqualTo(MAX_INTEGER_DIGITS);
        }

        @Test
        void ledgerLineForNonExistentAccountIsRejected() {
            UUID entryId = insertEntry("TEST");
            assertThatThrownBy(() -> insertLedgerLine(UUID.randomUUID(), entryId, 10))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_journal_lines_accounts");
        }

        @Test
        void ledgerLineForNonExistentEntryIsRejected() {
            UUID accountId = insertAccount("EUR");
            assertThatThrownBy(() -> insertLedgerLine(accountId, UUID.randomUUID(), 10))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_journal_lines_journal_entries");
        }

        @Test
        void sameAccountTwiceInOneEntryRejected() {
            UUID accountId = insertAccount("EUR");
            UUID entryId = insertEntry("TEST");
            insertLedgerLine(accountId, entryId, 10);
            assertThatThrownBy(() -> insertLedgerLine(accountId, entryId, -10))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_journal_lines_entry_account");
        }

        @Test
        void sameAccountInDifferentEntriesAllowed() {
            UUID accountId = insertAccount("EUR");
            UUID entryId1 = insertEntry("TEST");
            UUID entryId2 = insertEntry("TEST");
            insertLedgerLine(accountId, entryId1, 10);
            UUID savedAccount = assertDoesNotThrow(() -> jdbcTemplate.queryForObject("INSERT INTO journal_lines (account_id,entry_id , amount) VALUES (?,?,?) RETURNING account_id",
                    UUID.class, accountId, entryId2, -20));
            assertThat(savedAccount).isEqualTo(accountId);
        }
    }

    @Nested
    @DisplayName("Journal entries")
    class JournalEntry {
        @Test
        void descriptionNullRejected() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO journal_entries (description) VALUES (NULL)"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("violates not-null constraint");
        }

        @Test
        void descriptionOverMaxLengthRejected() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO journal_entries (description) VALUES (?)", "A".repeat(MAX_DESCRIPTION_LENGTH + 1)))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("value too long for type character varying(255)");
        }

        @Test
        void descriptionAtMaxLengthAccepted() {
            String savedDescription = assertDoesNotThrow(() -> jdbcTemplate.queryForObject("INSERT INTO journal_entries (description) VALUES (?) RETURNING description",
                    String.class, "A".repeat(MAX_DESCRIPTION_LENGTH)));
            assertThat(savedDescription).isEqualTo("A".repeat(MAX_DESCRIPTION_LENGTH));
        }
    }
}
