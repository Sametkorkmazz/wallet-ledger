package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
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
        LedgerLine ledgerLine1 = new LedgerLine(increased, Money.of(Currency.EUR, amount));
        LedgerLine ledgerLine2 = new LedgerLine(decreased, Money.of(Currency.EUR, "-" + amount));
        return LedgerEntry.of("TEST", EntryType.TRANSFER, null, List.of(ledgerLine1, ledgerLine2));
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

        @Test
        void savesEntryTypeLinkAndCreator() {
            UUID userId1 = TestData.insertUser(jdbcTemplate);
            UUID userId2 = TestData.insertUser(jdbcTemplate);
            UUID account1 = TestData.insertAccount(jdbcTemplate, userId1);
            UUID account2 = TestData.insertAccount(jdbcTemplate, userId2);

            LedgerLine line1 = new LedgerLine(account1, Money.of(Currency.EUR, "10"));
            LedgerLine line2 = new LedgerLine(account2, Money.of(Currency.EUR, "-10"));
            LedgerEntry ledgerEntryTransfer = LedgerEntry.of("TEST", EntryType.TRANSFER, userId1, List.of(line1, line2));
            UUID savedTransferId = ledgerStore.saveEntry(ledgerEntryTransfer);

            LedgerLine lineReversal1 = new LedgerLine(account1, Money.of(Currency.EUR, "-10"));
            LedgerLine lineReversal2 = new LedgerLine(account2, Money.of(Currency.EUR, "10"));
            LedgerEntry ledgerEntryReversal = new LedgerEntry("TEST", EntryType.REVERSAL, savedTransferId, userId2, List.of(lineReversal1, lineReversal2));
            UUID savedReversalId = ledgerStore.saveEntry(ledgerEntryReversal);

            UUID transferUser = jdbcTemplate.queryForObject("SELECT created_by FROM journal_entries WHERE id = ?", UUID.class, savedTransferId);
            UUID reversalUser = jdbcTemplate.queryForObject("SELECT created_by FROM journal_entries WHERE id = ?", UUID.class, savedReversalId);
            String transferType = jdbcTemplate.queryForObject("SELECT entry_type FROM journal_entries WHERE id = ?", String.class, savedTransferId);
            String reversalEntry = jdbcTemplate.queryForObject("SELECT entry_type FROM journal_entries WHERE id = ?", String.class, savedReversalId);
            UUID reversalType = jdbcTemplate.queryForObject("SELECT reverses_entry_id FROM journal_entries WHERE id = ?", UUID.class, savedReversalId);

            assertThat(transferUser).isEqualTo(userId1);
            assertThat(transferType).isEqualTo(EntryType.TRANSFER.name());
            assertThat(reversalUser).isEqualTo(userId2);
            assertThat(reversalType).isEqualTo(savedTransferId);
            assertThat(reversalEntry).isEqualTo(EntryType.REVERSAL.name());
        }
    }

    @Nested
    @DisplayName("Balance of")
    class BalanceOf {
        @Test
        void returnsZeroWhenAccountHasNoLines() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            UUID account3 = TestData.insertAccount(jdbcTemplate);
            LedgerEntry ledgerEntry = createTransferEntry(account1, account2, "10");
            ledgerStore.saveEntry(ledgerEntry);
            BigDecimal sum = ledgerStore.balanceOf(account3);
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
