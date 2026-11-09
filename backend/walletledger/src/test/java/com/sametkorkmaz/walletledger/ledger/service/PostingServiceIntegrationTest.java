package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import com.sametkorkmaz.walletledger.ledger.persistence.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class PostingServiceIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PostingService postingService;
    @Autowired
    LedgerStore ledgerStore;

    private static LineRequest createLineRequest(UUID accountId, BigDecimal amount) {
        return new LineRequest(accountId, amount);
    }

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
            LineRequest lineRequest1 = createLineRequest(account1, new BigDecimal("100"));
            LineRequest lineRequest2 = createLineRequest(account2, new BigDecimal("-100"));
            postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2));
            BigDecimal line1Balance = ledgerStore.balanceOf(account1);
            BigDecimal line2Balance = ledgerStore.balanceOf(account2);
            assertThat(line1Balance).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(line2Balance).isEqualByComparingTo(new BigDecimal("-100"));
        }

        @Test
        void savesNothingWhenAnAccountIsUnknown() {
            UUID account1 = TestData.insertAccount(jdbcTemplate);
            LineRequest lineRequest1 = createLineRequest(account1, new BigDecimal("100"));
            LineRequest lineRequest2 = createLineRequest(UUID.randomUUID(), new BigDecimal("-100"));
            assertThatThrownBy(() -> postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(AccountNotFoundException.class);
            int lines = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM journal_lines", Integer.class);
            int entries = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM journal_entries", Integer.class);
            assertThat(lines).isZero();
            assertThat(entries).isZero();
        }
    }
}
