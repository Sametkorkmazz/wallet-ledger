package com.sametkorkmaz.walletledger.ledger.domain;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LedgerEntryTest {
    // mirrors VARCHAR(255) in V2__ledger.sql
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    private LedgerLine line(Currency c, String amount) {
        return new LedgerLine(UUID.randomUUID(), Money.of(c, amount));
    }

    private LedgerEntry entry(EntryType entryType, UUID reversesEntryId) {
        LedgerLine line1 = line(Currency.EUR, "10");
        LedgerLine line2 = line(Currency.EUR, "-10");
        return new LedgerEntry("TEST", entryType, reversesEntryId, null, List.of(line1, line2));
    }

    private LedgerEntry entry(List<LedgerLine> lines) {
        return LedgerEntry.of("TEST", EntryType.TRANSFER, null, lines);
    }

    private LedgerEntry entry(String description, List<LedgerLine> lines) {
        return LedgerEntry.of(description, EntryType.TRANSFER, null, lines);
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
        void acceptsDescriptionAtMaxLength() {
            LedgerLine line1 = line(Currency.EUR, "5");
            LedgerLine line2 = line(Currency.EUR, "-5");
            LedgerEntry ledgerEntry = assertDoesNotThrow(() -> entry("A".repeat(MAX_DESCRIPTION_LENGTH), List.of(line1, line2)));
            assertThat(ledgerEntry.description()).hasSize(MAX_DESCRIPTION_LENGTH);
        }

        @Test
        void acceptsTrimmedDescriptionAtMaxLength() {
            LedgerLine line1 = line(Currency.EUR, "5");
            LedgerLine line2 = line(Currency.EUR, "-5");
            LedgerEntry ledgerEntry = assertDoesNotThrow(() -> entry(" " + "A".repeat(MAX_DESCRIPTION_LENGTH) + " ", List.of(line1, line2)));
            assertThat(ledgerEntry.description()).isEqualTo("A".repeat(MAX_DESCRIPTION_LENGTH));
        }

        @Test
        void returnsTrimmedDescription() {
            LedgerLine line1 = line(Currency.EUR, "5");
            LedgerLine line2 = line(Currency.EUR, "-5");
            LedgerEntry ledgerEntry = entry("  top-up  ", List.of(line1, line2));
            assertThat(ledgerEntry.description()).isEqualTo("top-up");
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

    @Nested
    @DisplayName("Description")
    class Description {
        @Test
        void rejectsNullDescription() {
            List<LedgerLine> lines = new ArrayList<>();
            lines.add(line(Currency.EUR, "-5"));
            lines.add(line(Currency.EUR, "5"));
            assertThatThrownBy(() -> entry(null, lines))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("description must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        void rejectsBlankDescription(String description) {
            List<LedgerLine> lines = new ArrayList<>();
            lines.add(line(Currency.EUR, "-5"));
            lines.add(line(Currency.EUR, "5"));
            assertThatThrownBy(() -> entry(description, lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("description must not be blank");
        }

        @Test
        void rejectsDescriptionOverMaxLength() {
            List<LedgerLine> lines = new ArrayList<>();
            lines.add(line(Currency.EUR, "-5"));
            lines.add(line(Currency.EUR, "5"));
            assertThatThrownBy(() -> entry("1".repeat(MAX_DESCRIPTION_LENGTH) + "2", lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("description must be at most %d characters".formatted(MAX_DESCRIPTION_LENGTH));
        }
    }

    @Nested
    @DisplayName("Line count")
    class LineCount {
        @Test
        void rejectsNullLines() {
            assertThatThrownBy(() -> entry(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("lines must not be null");
        }

        @Test
        void rejectsNullLedgerLineInLines() {
            List<LedgerLine> lines = Arrays.asList(
                    line(Currency.EUR, "10"),
                    line(Currency.EUR, "-10"),
                    null
            );
            assertThatThrownBy(() -> entry(lines))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("line must not be null");
        }

        @Test
        void rejectsEmptyLines() {
            List<LedgerLine> lines = new ArrayList<>();
            assertThatThrownBy(() -> entry(lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry must have at least 2 lines");
        }

        @Test
        void rejectsSingleLine() {
            List<LedgerLine> lines = new ArrayList<>();
            lines.add(line(Currency.EUR, "10"));
            assertThatThrownBy(() -> entry(lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry must have at least 2 lines");
        }

        @Test
        void rejectsDuplicateAccountInLines() {
            UUID id = UUID.randomUUID();
            List<LedgerLine> lines = new ArrayList<>();
            lines.add(new LedgerLine(id, Money.of(Currency.EUR, "-5")));
            lines.add(new LedgerLine(id, Money.of(Currency.EUR, "-5")));
            lines.add(line(Currency.EUR, "10"));

            assertThatThrownBy(() -> entry(lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry must not contain duplicate accounts");
        }
    }

    @Nested
    @DisplayName("Balance")
    class Balance {
        @Test
        void rejectsEntryThatDoesNotSumToZero() {
            LedgerLine line1 = line(Currency.USD, "-5");
            LedgerLine line2 = line(Currency.USD, "10");
            assertThatThrownBy(() -> entry(List.of(line1, line2)))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry does not balance for " + Currency.USD + ": total = 5.00");
        }

        @Test
        void rejectsEntryBalancedOnlyAcrossCurrencies() {
            LedgerLine line1 = line(Currency.EUR, "10");
            LedgerLine line2 = line(Currency.USD, "-10");
            assertThatThrownBy(() -> entry(List.of(line1, line2)))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry does not balance for EUR: total = 10.00");
        }

        @Test
        void acceptsEntryBalancedInEachCurrency() {
            LedgerLine line1 = line(Currency.USD, "10");
            LedgerLine line2 = line(Currency.USD, "-10");
            LedgerLine line3 = line(Currency.EUR, "30");
            LedgerLine line4 = line(Currency.EUR, "-30");
            assertDoesNotThrow(() -> entry(List.of(line1, line2, line3, line4)));
        }

        @Test
        void rejectsEntryNotBalancedInEachCurrency() {
            LedgerLine line1 = line(Currency.USD, "10");
            LedgerLine line2 = line(Currency.USD, "-10");
            LedgerLine line3 = line(Currency.EUR, "20");
            LedgerLine line4 = line(Currency.EUR, "-30");
            assertThatThrownBy(() -> entry(List.of(line1, line2, line3, line4)))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry does not balance for EUR: total = -10.00");
        }
    }

    @Nested
    @DisplayName("Entry type")
    class EntryTypeTest {
        @ParameterizedTest
        @EnumSource(value = EntryType.class, names = {"TRANSFER", "TOP_UP", "SYSTEM"})
        void acceptsNullReversesEntryIdForNonReversalTypes(EntryType entryType) {
            LedgerEntry ledgerEntry = entry(entryType, null);
            assertThat(ledgerEntry.entryType()).isEqualTo(entryType);
            assertThat(ledgerEntry.reversesEntryId()).isNull();
        }

        @Test
        void returnsReversesEntryIdForReversal() {
            UUID reversesEntryId = UUID.randomUUID();
            LedgerEntry ledgerEntry = entry(EntryType.REVERSAL, reversesEntryId);
            assertThat(ledgerEntry.entryType()).isEqualTo(EntryType.REVERSAL);
            assertThat(ledgerEntry.reversesEntryId()).isEqualTo(reversesEntryId);
        }

        @Test
        void rejectsNullReversesEntryIdForReversal() {
            assertThatThrownBy(() -> entry(EntryType.REVERSAL, null))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessage("reversesEntryId is required for REVERSAL and not allowed for other types");
        }

        @ParameterizedTest
        @EnumSource(value = EntryType.class, names = {"TRANSFER", "TOP_UP", "SYSTEM"})
        void rejectsReversesEntryIdForNonReversalTypes(EntryType entryType) {
            assertThatThrownBy(() -> entry(entryType, UUID.randomUUID()))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessage("reversesEntryId is required for REVERSAL and not allowed for other types");
        }

        @Test
        void rejectsNullEntryType() {
            assertThatThrownBy(() -> entry((EntryType) null, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("entryType must not be null");
        }
    }
}
