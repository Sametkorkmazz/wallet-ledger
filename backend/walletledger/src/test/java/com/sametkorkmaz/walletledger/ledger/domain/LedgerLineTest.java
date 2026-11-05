package com.sametkorkmaz.walletledger.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerLineTest {

    private LedgerLine line(Currency c, String amount) {
        return new LedgerLine(UUID.randomUUID(), c, new BigDecimal(amount));
    }

    @Nested
    @DisplayName("Currency")
    class CurrencyTest {
        @Test
        void rejectsNullCurrency() {
            assertThatThrownBy(() -> line(null, "10"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currency must not be null");
        }
    }

    @Nested
    @DisplayName("Amount")
    class Amount {
        @Test
        void rejectsNullAmount() {
            assertThatThrownBy(() -> new LedgerLine(UUID.randomUUID(), Currency.EUR, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("amount must not be null");
        }

        @Test
        void rejectsZeroAmount() {
            assertThatThrownBy(() -> line(Currency.EUR, "0"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must not be zero");

        }
    }
}
