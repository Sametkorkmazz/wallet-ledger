package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JournalLineEntityTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final UUID ENTRY_ID = UUID.randomUUID();
    private static final BigDecimal AMOUNT = BigDecimal.TEN;

    private static JournalLineEntity createLine() {
        return new JournalLineEntity(ACCOUNT_ID, ENTRY_ID, AMOUNT);
    }

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {
        @Test
        void keepsAccountIdEntryIdAndAmount() {
            JournalLineEntity journalLineEntity = createLine();
            assertThat(journalLineEntity.getAccountId()).isEqualTo(ACCOUNT_ID);
            assertThat(journalLineEntity.getEntryId()).isEqualTo(ENTRY_ID);
            assertThat(journalLineEntity.getAmount()).isEqualByComparingTo(AMOUNT);
        }
    }

    @Nested
    @DisplayName("Null arguments")
    class NullArguments {
        @Test
        void rejectsNullAccountId() {
            assertThatThrownBy(() -> new JournalLineEntity(null, ENTRY_ID, AMOUNT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accountId must not be null");
        }

        @Test
        void rejectsNullEntryId() {
            assertThatThrownBy(() -> new JournalLineEntity(ACCOUNT_ID, null, AMOUNT))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("entryId must not be null");
        }

        @Test
        void rejectsNullAmount() {
            assertThatThrownBy(() -> new JournalLineEntity(ACCOUNT_ID, ENTRY_ID, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("amount must not be null");
        }
    }

    @Nested
    @DisplayName("Amount validation")
    class AmountValidation {
        @Test
        void acceptsExactlyFourDecimalPlaces() {
            JournalLineEntity journalLineEntity = assertDoesNotThrow(() -> new JournalLineEntity(ACCOUNT_ID, ENTRY_ID, new BigDecimal("10.0001")));
            assertThat(journalLineEntity.getAmount()).isEqualByComparingTo(new BigDecimal("10.0001"));
        }

        @Test
        void rejectsMoreThanFourDecimalPlaces() {
            assertThatThrownBy(() -> new JournalLineEntity(ACCOUNT_ID, ENTRY_ID, new BigDecimal("10.00001")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most 4 decimal places");
        }
    }

    @Nested
    @DisplayName("Amount normalization")
    class AmountNormalization {
        @Test
        void storesAmountWithScaleFour() {
            JournalLineEntity journalLineEntity = new JournalLineEntity(ACCOUNT_ID, ENTRY_ID, new BigDecimal("2"));
            assertThat(journalLineEntity.getAmount().scale()).isEqualTo(LedgerLine.MAX_SCALE);
        }
    }
}
