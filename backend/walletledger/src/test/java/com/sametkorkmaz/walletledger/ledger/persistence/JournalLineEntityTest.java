package com.sametkorkmaz.walletledger.ledger.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.sametkorkmaz.walletledger.ledger.persistence.LedgerLineData.*;
import static org.assertj.core.api.Assertions.assertThat;

class JournalLineEntityTest {

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {
        @Test
        void keepsAccountIdEntryIdAndAmount() {
            JournalLineEntity journalLineEntity = create("10");
            assertThat(journalLineEntity.getAccountId()).isEqualTo(ACCOUNT_ID.value());
            assertThat(journalLineEntity.getEntryId()).isEqualTo(ENTRY_ID.value());
            assertThat(journalLineEntity.getAmount()).isEqualByComparingTo("10");
        }
    }

    @Nested
    @DisplayName("Amount normalization")
    class AmountNormalization {
        @Test
        void storesAmountWithScaleFour() {
            JournalLineEntity journalLineEntity = create("2");
            assertThat(journalLineEntity.getAmount().scale()).isEqualTo(4);
        }
    }
}
