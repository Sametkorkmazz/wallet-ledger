package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
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

    @BeforeEach
    void cleanTables() {
        jdbcTemplate.update("DELETE from journal_lines");
        jdbcTemplate.update("DELETE from journal_entries");
        jdbcTemplate.update("DELETE from accounts");
        jdbcTemplate.update("DELETE from users");
    }

    @Nested
    @DisplayName("Transaction behaviour")
    class TransactionBehaviour {

        @Test
        void savesEntryAndAllLinesTogether() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            UUID account2 = TestData.insertAccount(jdbcTemplate);
            LedgerLine ledgerLine1 = new LedgerLine(account1, Currency.EUR, new BigDecimal("10.0"));
            LedgerLine ledgerLine2 = new LedgerLine(account2, Currency.EUR, new BigDecimal("-10.0"));
            LedgerEntry ledgerEntry = new LedgerEntry("TEST", List.of(ledgerLine1, ledgerLine2));
            ledgerStore.saveEntry(ledgerEntry);
            int entries = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_entries", Integer.class);
            int lines = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_lines", Integer.class);
            assertThat(entries).isEqualTo(1);
            assertThat(lines).isEqualTo(2);
        }

        @Test
        void rollsBackEntryAndLinesWhenALineFails() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            LedgerLine ledgerLine1 = new LedgerLine(account1, Currency.EUR, new BigDecimal("10.0"));
            LedgerLine ledgerLine2 = new LedgerLine(UUID.randomUUID(), Currency.EUR, new BigDecimal("-10.0"));
            LedgerEntry ledgerEntry = new LedgerEntry("TEST", List.of(ledgerLine1, ledgerLine2));
            assertThatThrownBy(() -> ledgerStore.saveEntry(ledgerEntry))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("fk_journal_lines_accounts");
            int entries = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_entries", Integer.class);
            int lines = jdbcTemplate.queryForObject("SELECT count(*) FROM journal_lines", Integer.class);
            assertThat(entries).isZero();
            assertThat(lines).isZero();
        }
    }
}
