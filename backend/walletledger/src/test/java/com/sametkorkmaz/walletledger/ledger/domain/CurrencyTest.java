package com.sametkorkmaz.walletledger.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyTest {

    @Nested
    @DisplayName("Column scale")
    class ColumnScale {
        @Test
        void everyCurrencyFitsTheColumnScale() {
            for (Currency currency : Currency.values()) {
                assertThat(currency.minorUnits())
                        .as(currency.name())
                        .isBetween(0, 4); // NUMERIC(19, 4)
            }
        }
    }
}
