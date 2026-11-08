package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Import({TestcontainersConfiguration.class, LedgerStore.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class LedgerStoreTest {

    @Autowired
    LedgerStore ledgerStore;
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static LedgerEntry createTransferEntry(UUID increased, UUID decreased, String amount) {
        LedgerLine ledgerLine1 = new LedgerLine(increased, Currency.EUR, new BigDecimal(amount));
        LedgerLine ledgerLine2 = new LedgerLine(decreased, Currency.EUR, new BigDecimal("-" + amount));
        return new LedgerEntry("TEST", List.of(ledgerLine1, ledgerLine2));
    }

    @Nested
    @DisplayName("Save entry")
    class SaveEntry {
        @Test
        void returnsIdOfSavedEntry() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry = createTransferEntry(account1, account2, "10");
            UUID savedId = ledgerStore.saveEntry(ledgerEntry);
            int count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM journal_entries WHERE id = ?", Integer.class, savedId);
            assertThat(count).isEqualTo(1);
        }

        @Test
        void savesEntryWithDescription() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry = createTransferEntry(account1, account2, "10");
            UUID savedId = ledgerStore.saveEntry(ledgerEntry);
            String description = jdbcTemplate.queryForObject("SELECT description FROM journal_entries WHERE id = ?", String.class, savedId);
            assertThat(description).isEqualTo("TEST");
        }

        @Test
        void savesOneLinePerLedgerLine() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry = createTransferEntry(account1, account2, "10");
            UUID savedId = ledgerStore.saveEntry(ledgerEntry);
            int lines = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_lines WHERE entry_id = ?", Integer.class, savedId);
            BigDecimal amount1 = jdbcTemplate.queryForObject("SELECT amount FROM journal_lines WHERE account_id = ? and entry_id = ?", BigDecimal.class, account1, savedId);
            BigDecimal amount2 = jdbcTemplate.queryForObject("SELECT amount FROM journal_lines WHERE account_id = ? and entry_id = ?", BigDecimal.class, account2, savedId);

            assertThat(lines).isEqualTo(2);
            assertThat(amount1).isEqualByComparingTo("10");
            assertThat(amount2).isEqualByComparingTo("-10");
        }
    }

    @Nested
    @DisplayName("Balance of")
    class BalanceOf {
        @Test
        void returnsZeroForAccountWithoutLines() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry = createTransferEntry(account1, account2, "10");
            ledgerStore.saveEntry(ledgerEntry);
            BigDecimal sum = ledgerStore.balanceOf(UUID.randomUUID());
            assertThat(sum).isEqualByComparingTo(new BigDecimal("0"));
        }

        @Test
        void sumsBalanceOfOnlyRequestedAccount() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry = createTransferEntry(account1, account2, "10");
            ledgerStore.saveEntry(ledgerEntry);
            BigDecimal sum = ledgerStore.balanceOf(account1);
            assertThat(sum).isEqualByComparingTo(new BigDecimal("10"));
        }

        @Test
        void sumsPositiveAndNegativeAmountsOfAccount() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry1 = createTransferEntry(account1, account2, "10");
            LedgerEntry ledgerEntry2 = createTransferEntry(account2, account1, "30");
            ledgerStore.saveEntry(ledgerEntry1);
            ledgerStore.saveEntry(ledgerEntry2);
            BigDecimal sum = ledgerStore.balanceOf(account1);
            assertThat(sum).isEqualByComparingTo(new BigDecimal("-20"));
        }
    }
}
