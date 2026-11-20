package com.sametkorkmaz.walletledger;

import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.persistence.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
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

    @BeforeEach
    void cleanTables() {
        TestData.cleanTables(jdbcTemplate);
    }

    private UUID insertAccount(String currency) {
        return jdbcTemplate.queryForObject("INSERT INTO accounts (currency) VALUES (?) RETURNING id", UUID.class,
                currency);
    }

    private UUID insertEntry(String description) {
        return jdbcTemplate.queryForObject("INSERT INTO journal_entries (description, entry_type) VALUES (?, 'TRANSFER') RETURNING id", UUID.class,
                description);
    }

    private UUID insertEntry(String entryType, UUID reversesEntryId) {
        return jdbcTemplate.queryForObject("INSERT INTO journal_entries (description, entry_type, reverses_entry_id) VALUES ('TEST', ?, ?) RETURNING id", UUID.class,
                entryType, reversesEntryId);
    }

    private void insertKey(UUID userId, UUID key) {
        jdbcTemplate.update("INSERT INTO idempotency_keys (user_id, idempotency_key, request_hash) VALUES (?, ?, ?)",
                userId, key, "a".repeat(64));
    }

    @Nested
    @DisplayName("Users table")
    class UsersTable {
        @Test
        void rejectsDuplicateEmail() {
            jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "test@example.com", "test123");
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "test@example.com", "test456"))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void rejectsSameEmailInDifferentCase() {
            jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "test@example.com", "test123");
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO users (email, password_hash) VALUES (?, ?)", "TEST@example.com", "test456"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_users_email_lower");
        }
    }

    @Nested
    @DisplayName("Accounts table")
    class AccountsTable {
        @Test
        void rejectsAccountForNonExistentUser() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", UUID.randomUUID()))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_accounts_users");
        }

        @ParameterizedTest
        @ValueSource(strings = {"EUR", "USD", "TRY"})
        void acceptsValidCurrency(String currency) {
            assertDoesNotThrow(() -> jdbcTemplate.update("INSERT INTO accounts (currency) VALUES (?)", currency));
            int count = jdbcTemplate.queryForObject("SELECT count(*) FROM accounts", Integer.class);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void rejectsInvalidCurrency() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (currency) VALUES ('XYZ')"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_accounts_currency");
        }

        @Test
        void rejectsSameAccountInSameCurrency() {
            UUID userId = jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash) VALUES (?, ?) RETURNING id",
                    UUID.class, "a@test.com", "hash");
            jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", userId);
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (?,'EUR')", userId))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_accounts_user_currency");
        }

        @Test
        void rejectsSecondExternalAccountInSameCurrency() {
            jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (NULL,'EUR')");
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO accounts (user_id, currency) VALUES (NULL,'EUR')"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_accounts_external_currency");
        }
    }

    @Nested
    @DisplayName("Journal lines table")
    class JournalLinesTable {
        private void insertLedgerLine(UUID accountId, UUID entryId, int amount) {
            jdbcTemplate.update("INSERT INTO journal_lines (account_id, entry_id, amount) VALUES (?,?,?)",
                    accountId, entryId, amount);
        }

        @Test
        void rejectsZeroAmount() {
            UUID accountId = insertAccount("EUR");
            UUID entryId = insertEntry("TEST");
            assertThatThrownBy(() -> insertLedgerLine(accountId, entryId, 0))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_journal_lines_amount_not_zero");
        }

        @Test
        void storesFourDecimalPlaces() {
            UUID accountId = insertAccount("EUR");
            UUID entryId = insertEntry("TEST");
            UUID lineId = jdbcTemplate.queryForObject("INSERT INTO journal_lines (account_id, entry_id, amount) VALUES (?,?,?) RETURNING id",
                    UUID.class, accountId, entryId, new BigDecimal("10.0001"));
            BigDecimal amount = jdbcTemplate.queryForObject("SELECT amount from journal_lines where id = ?",
                    BigDecimal.class, lineId);
            assertThat(amount).isEqualTo(new BigDecimal("10.0001"));
        }

        @Test
        void definesAmountColumnWithDomainLimits() {
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
        void rejectsLineForNonExistentAccount() {
            UUID entryId = insertEntry("TEST");
            assertThatThrownBy(() -> insertLedgerLine(UUID.randomUUID(), entryId, 10))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_journal_lines_accounts");
        }

        @Test
        void rejectsLineForNonExistentEntry() {
            UUID accountId = insertAccount("EUR");
            assertThatThrownBy(() -> insertLedgerLine(accountId, UUID.randomUUID(), 10))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_journal_lines_journal_entries");
        }

        @Test
        void rejectsSameAccountTwiceInOneEntry() {
            UUID accountId = insertAccount("EUR");
            UUID entryId = insertEntry("TEST");
            insertLedgerLine(accountId, entryId, 10);
            assertThatThrownBy(() -> insertLedgerLine(accountId, entryId, -10))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_journal_lines_entry_account");
        }

        @Test
        void acceptsSameAccountInDifferentEntries() {
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
    @DisplayName("Journal entries table")
    class JournalEntriesTable {
        @Test
        void rejectsNullDescription() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO journal_entries (description, entry_type) VALUES (NULL, 'TRANSFER')"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("violates not-null constraint")
                    .hasMessageContaining("null value in column \"description\"");
        }

        @Test
        void rejectsDescriptionOverMaxLength() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO journal_entries (description, entry_type) VALUES (?, 'TRANSFER')", "A".repeat(MAX_DESCRIPTION_LENGTH + 1)))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("value too long for type character varying(255)");
        }

        @Test
        void acceptsDescriptionAtMaxLength() {
            String savedDescription = assertDoesNotThrow(() -> jdbcTemplate.queryForObject("INSERT INTO journal_entries (description, entry_type) VALUES (?, 'TRANSFER') RETURNING description",
                    String.class, "A".repeat(MAX_DESCRIPTION_LENGTH)));
            assertThat(savedDescription).isEqualTo("A".repeat(MAX_DESCRIPTION_LENGTH));
        }

        @ParameterizedTest
        @EnumSource(value = EntryType.class, names = {"TRANSFER", "TOP_UP", "SYSTEM"})
        void acceptsValidEntryTypes(EntryType entryType) {
            UUID saved = insertEntry(entryType.name(), null);
            assertThat(saved).isNotNull();
        }

        @Test
        void rejectsUnknownEntryType() {
            assertThatThrownBy(() -> insertEntry("INVALID", null))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_journal_entries_entry_type");
        }

        @Test
        void rejectsMissingEntryType() {
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO journal_entries (description) VALUES ('TEST')"))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("violates not-null constraint")
                    .hasMessageContaining("null value in column \"entry_type\"");
        }

        @Test
        void rejectsReversalWithoutLink() {
            assertThatThrownBy(() -> insertEntry(EntryType.REVERSAL.name(), null))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_journal_entries_reversal_link");
        }

        @Test
        void rejectsLinkWithoutReversal() {
            UUID saved = insertEntry(EntryType.TRANSFER.name(), null);
            assertThatThrownBy(() -> insertEntry(EntryType.TRANSFER.name(), saved))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_journal_entries_reversal_link");
        }

        @Test
        void acceptsReversal() {
            UUID saved = insertEntry(EntryType.TRANSFER.name(), null);
            UUID reversesId = insertEntry(EntryType.REVERSAL.name(), saved);
            assertThat(reversesId).isNotNull();
        }

        @Test
        void rejectsSecondReversalOfSameEntry() {
            UUID saved = insertEntry(EntryType.TRANSFER.name(), null);
            insertEntry(EntryType.REVERSAL.name(), saved);
            assertThatThrownBy(() -> insertEntry(EntryType.REVERSAL.name(), saved))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_journal_entries_reverses_entry_id");
        }

        @Test
        void rejectsSelfReversal() {
            UUID id = UUID.randomUUID();
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO journal_entries (id, description, entry_type, reverses_entry_id) VALUES (?, 'TEST', 'REVERSAL', ?)", id, id))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("ck_journal_entries_not_self_reversal");
        }
    }

    @Nested
    @DisplayName("Idempotency keys table")
    class IdempotencyKeysTable {
        @Test
        void acceptsKeyRowWithoutResponse() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            UUID key = UUID.randomUUID();
            insertKey(userId, key);
            assertThat(jdbcTemplate.queryForObject("SELECT entry_id FROM idempotency_keys WHERE user_id = ? AND idempotency_key = ?", UUID.class, userId, key)).isNull();
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id = ? AND idempotency_key = ?", Integer.class,
                    userId, key);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void rejectsDuplicateKeyForSameUser() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            UUID key = UUID.randomUUID();
            insertKey(userId, key);
            assertThatThrownBy(() -> insertKey(userId, key))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("pk_idempotency_keys");
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id = ? AND idempotency_key = ?", Integer.class,
                    userId, key);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void acceptsSameKeyForDifferentUsers() {
            UUID userId1 = TestData.insertUser(jdbcTemplate);
            UUID userId2 = TestData.insertUser(jdbcTemplate);
            UUID key = UUID.randomUUID();
            insertKey(userId1, key);
            insertKey(userId2, key);
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE idempotency_key = ?", Integer.class, key);
            assertThat(count).isEqualTo(2);
        }

        @Test
        void rejectsMissingRequestHash() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            UUID key = UUID.randomUUID();
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO idempotency_keys (user_id, idempotency_key) VALUES (?, ?)", userId, key))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("violates not-null constraint")
                    .hasMessageContaining("null value in column \"request_hash\"");
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE idempotency_key = ? AND user_id = ?", Integer.class, key, userId);
            assertThat(count).isEqualTo(0);
        }

        @Test
        void rejectsUnknownUser() {
            UUID key = UUID.randomUUID();
            UUID unknownUserId = UUID.randomUUID();
            assertThatThrownBy(() -> insertKey(unknownUserId, key))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_idempotency_keys_users");
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id = ? AND idempotency_key = ?", Integer.class, unknownUserId, key);
            assertThat(count).isEqualTo(0);
        }

        @Test
        void rejectsUnknownEntry() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            UUID key = UUID.randomUUID();
            assertThatThrownBy(() -> jdbcTemplate.update("INSERT INTO idempotency_keys (user_id, idempotency_key, request_hash, entry_id) VALUES (?, ?, ?, ?)",
                    userId, key, "a".repeat(64), UUID.randomUUID()))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_idempotency_keys_journal_entries");
            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM idempotency_keys WHERE user_id = ? AND idempotency_key = ?", Integer.class, userId, key);
            assertThat(count).isEqualTo(0);
        }
    }
}
