package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LedgerStoreIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    LedgerStore ledgerStore;
    @Autowired
    TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanTables() {
        TestData.cleanTables(jdbcTemplate);
    }

    @Nested
    @DisplayName("Transaction behaviour")
    class TransactionBehaviour {

        @Test
        void savesEntryAndAllLinesTogether() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerLine ledgerLine1 = new LedgerLine(AccountId.of(account1), Money.of(Currency.EUR, "10.0"));
            LedgerLine ledgerLine2 = new LedgerLine(AccountId.of(account2), Money.of(Currency.EUR, "-10.0"));
            LedgerEntry ledgerEntry = LedgerEntry.of("TEST", EntryType.TRANSFER, null, List.of(ledgerLine1, ledgerLine2));
            ledgerStore.saveEntry(ledgerEntry);
            int entries = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_entries", Integer.class);
            int lines = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_lines", Integer.class);
            assertThat(entries).isEqualTo(1);
            assertThat(lines).isEqualTo(2);
        }

        @Test
        void rollsBackEntryAndLinesWhenALineFailsInsideCallerTransaction() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            LedgerLine ledgerLine1 = new LedgerLine(AccountId.of(account1), Money.of(Currency.EUR, "10.0"));
            LedgerLine ledgerLine2 = new LedgerLine(AccountId.of(UUID.randomUUID()), Money.of(Currency.EUR, "-10.0"));
            LedgerEntry ledgerEntry = LedgerEntry.of("TEST", EntryType.TRANSFER, null, List.of(ledgerLine1, ledgerLine2));
            assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(s -> ledgerStore.saveEntry(ledgerEntry)))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_journal_lines_accounts");
            int entries = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_entries", Integer.class);
            int lines = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_lines", Integer.class);
            assertThat(entries).isZero();
            assertThat(lines).isZero();
        }
    }
}
