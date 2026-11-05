package com.sametkorkmaz.walletledger.ledger.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LedgerEntryTest {

    private LedgerLine line(Currency c, String amount) {
        return new LedgerLine(UUID.randomUUID(), c, new BigDecimal(amount));
    }

    private LedgerEntry entry(List<LedgerLine> lines) {
        return new LedgerEntry("TEST", lines);
    }

    private LedgerEntry entry(String description, List<LedgerLine> lines) {
        return new LedgerEntry(description, lines);
    }

    @Nested
    @DisplayName("Valid entries")
    class ValidEntries {
        @Test
        void acceptsBalancedTwoLineEntry() {
            LedgerLine line1 = line(Currency.EUR, "2");
            LedgerLine line2 = line(Currency.EUR, "-2");
            LedgerEntry ledgerEntry = assertDoesNotThrow(() -> entry(List.of(line1, line2)));
            assertThat(ledgerEntry.lines()).hasSize(2);
        }

        @Test
        void acceptsEntryWithThreeLines() {
            LedgerLine line1 = line(Currency.EUR, "5");
            LedgerLine line2 = line(Currency.EUR, "-2");
            LedgerLine line3 = line(Currency.EUR, "-3");
            assertDoesNotThrow(() -> entry(List.of(line1, line2, line3)));
        }

        @Test
        void acceptsAmountsThatNeedExactDecimalArithmetic() {
            LedgerLine line1 = line(Currency.EUR, "0.10");
            LedgerLine line2 = line(Currency.EUR, "0.20");
            LedgerLine line3 = line(Currency.EUR, "-0.30");
            assertDoesNotThrow(() -> entry(List.of(line1, line2, line3)));
        }

        @Test
        void keepsItsOwnCopyOfTheLines() {
            List<LedgerLine> lines = new ArrayList<>();
            lines.add(line(Currency.EUR, "5"));
            lines.add(line(Currency.EUR, "-5"));
            LedgerEntry ledgerEntry = entry(lines);
            lines.add(line(Currency.EUR, "-5"));
            assertThat(ledgerEntry.lines()).hasSize(2);
        }
    }
}
