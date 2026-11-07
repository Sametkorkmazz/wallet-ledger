package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalLineRepositoryTest {
    // mirrors NUMERIC(19,4) in V2__ledger.sql.
    private static final int MAX_SCALE = 4;
    @Autowired
    JournalLineRepository journalLineRepository;
    @Autowired
    TestEntityManager testEntityManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    private UUID insertUser() {
        return jdbcTemplate.queryForObject("INSERT INTO users (email,password_hash) VALUES  (?,'test123') RETURNING id", UUID.class, UUID.randomUUID() + "@test.com");
    }

    private UUID insertAccount() {
        UUID userId = insertUser();
        return jdbcTemplate.queryForObject("INSERT INTO accounts (user_id, currency) VALUES  (?,'EUR') RETURNING id", UUID.class, userId);
    }

    private UUID insertEntry() {
        return jdbcTemplate.queryForObject("INSERT INTO journal_entries (description) VALUES  ('test') RETURNING id", UUID.class);
    }

    private JournalLineEntity saveLine(UUID accountId, UUID entryId, String amount) {
        return journalLineRepository.saveAndFlush(new JournalLineEntity(accountId, entryId, new BigDecimal(amount)));
    }

    @Nested
    @DisplayName("Saving and reading")
    class SavingAndReading {
        @Test
        void savesJournalLine() {
            UUID accountId = insertAccount();
            UUID entryId = insertEntry();
            UUID savedId = saveLine(accountId, entryId, "-12.3456").getId();
            testEntityManager.clear();
            JournalLineEntity found = journalLineRepository.findById(savedId).orElseThrow();
            assertThat(found.getAccountId()).isEqualTo(accountId);
            assertThat(found.getEntryId()).isEqualTo(entryId);
            assertThat(found.getAmount()).isEqualTo(new BigDecimal("-12.3456"));
            assertThat(found.getAmount().scale()).isEqualTo(MAX_SCALE);
        }
    }

    @Nested
    @DisplayName("Finding lines by entry")
    class FindByEntryId {
        @Test
        void returnsOnlyLinesOfThatEntry() {
            UUID account1 = insertAccount();
            UUID account2 = insertAccount();
            UUID entryId1 = insertEntry();
            UUID entryId2 = insertEntry();
            saveLine(account1, entryId1, "-12.3456");
            saveLine(account2, entryId1, "5.302");
            saveLine(account2, entryId2, "100.20");
            testEntityManager.clear();
            List<JournalLineEntity> found = journalLineRepository.findByEntryId(entryId1);
            assertThat(found).extracting(JournalLineEntity::getAccountId).containsExactlyInAnyOrder(account1, account2);
            assertThat(found).extracting(JournalLineEntity::getEntryId).containsOnly(entryId1);
        }

        @Test
        void returnsEmptyListForUnknownEntry() {
            UUID account1 = insertAccount();
            UUID entryId1 = insertEntry();
            saveLine(account1, entryId1, "-12.3456");
            testEntityManager.clear();
            List<JournalLineEntity> found = journalLineRepository.findByEntryId(UUID.randomUUID());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Sum amount by account id")
    class SumAmountByAccountId {
        @Test
        void returnsZeroWhenNoLineFoundUnderAccount() {
            UUID account1 = insertAccount();
            UUID account2 = insertAccount();

            UUID entryId1 = insertEntry();
            UUID entryId2 = insertEntry();

            saveLine(account1, entryId1, "10.00");
            saveLine(account2, entryId1, "-10.00");

            saveLine(account1, entryId2, "30.00");
            saveLine(account2, entryId2, "-30.00");

            UUID account3 = insertAccount();
            testEntityManager.clear();
            BigDecimal sum = journalLineRepository.sumAmountByAccountId(account3);
            assertThat(sum).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void sumsPositiveAndNegativeAmounts() {
            UUID account1 = insertAccount();
            UUID account2 = insertAccount();

            UUID entryId1 = insertEntry();
            UUID entryId2 = insertEntry();

            saveLine(account1, entryId1, "105.00");
            saveLine(account2, entryId1, "-105.00");

            saveLine(account1, entryId2, "-20.00");
            saveLine(account2, entryId2, "20.00");

            testEntityManager.clear();
            BigDecimal sum = journalLineRepository.sumAmountByAccountId(account1);
            assertThat(sum).isEqualByComparingTo(new BigDecimal("85"));
        }

        @Test
        void sumsAmountOnlyOfThatAccount() {
            UUID account1 = insertAccount();
            UUID account2 = insertAccount();
            UUID account3 = insertAccount();
            UUID account4 = insertAccount();

            UUID entryId1 = insertEntry();
            UUID entryId2 = insertEntry();

            saveLine(account1, entryId1, "15.00");
            saveLine(account2, entryId1, "-15.00");
            saveLine(account3, entryId1, "40.00");
            saveLine(account4, entryId1, "-40.00");

            saveLine(account1, entryId2, "30.00");
            saveLine(account2, entryId2, "-30.00");

            testEntityManager.clear();
            BigDecimal sum = journalLineRepository.sumAmountByAccountId(account1);
            assertThat(sum).isEqualByComparingTo(new BigDecimal("45"));
        }
    }
}
