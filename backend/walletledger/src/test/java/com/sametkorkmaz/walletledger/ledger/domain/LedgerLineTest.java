package com.sametkorkmaz.walletledger.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerLineTest {
    private static final int MAX_SCALE = 4;
    private static final int MAX_DIGITS = 15;

    private LedgerLine line(Currency c, String amount) {
        return new LedgerLine(UUID.randomUUID(), c, new BigDecimal(amount));
    }

    private LedgerLine line(UUID accountId, Currency c, String amount) {
        return new LedgerLine(accountId, c, new BigDecimal(amount));
    }

    @Nested
    @DisplayName("Account")
    class Account {
        @Test
        void rejectsNullAccountId() {
            assertThatThrownBy(() -> new LedgerLine(null, Currency.EUR, BigDecimal.TEN))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accountId must not be null");
        }
    }

    @Nested
    @DisplayName("Currency")
    class CurrencyField {
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

        @Test
        void rejectsAmountOverMaxScale() {
            BigDecimal amount = new BigDecimal("10.12345");
            assertThatThrownBy(() -> new LedgerLine(UUID.randomUUID(), Currency.EUR, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most %d".formatted(MAX_SCALE));
        }

        @Test
        void rejectsAmountOverMaxDigits() {
            BigDecimal amount = new BigDecimal("1".repeat(MAX_DIGITS + 1) + ".123");
            assertThatThrownBy(() -> new LedgerLine(UUID.randomUUID(), Currency.EUR, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most %d".formatted(MAX_DIGITS));
        }

        @Test
        void acceptsAmountAtMaxDigits() {
            LedgerLine ledgerLine = line(Currency.EUR, "1E+14");
            int digits = ledgerLine.amount().precision() - ledgerLine.amount().scale();
            assertThat(digits).isEqualTo(MAX_DIGITS);
        }

        @Test
        void acceptsAmountAtMaxScale() {
            LedgerLine ledgerLine = line(Currency.EUR, "10.1234");
            assertThat(ledgerLine.amount().scale()).isEqualTo(MAX_SCALE);
            assertThat(ledgerLine.amount()).isEqualTo(new BigDecimal("10.1234"));
        }

        @Test
        void acceptsSmallestValue() {
            LedgerLine ledgerLine = line(Currency.EUR, "0.0001");
            assertThat(ledgerLine.amount()).isEqualTo(new BigDecimal("0.0001"));
        }

        @Test
        void rejectsAmountBelowSmallestUnit() {
            BigDecimal amount = new BigDecimal("0.00001");
            assertThatThrownBy(() -> new LedgerLine(UUID.randomUUID(), Currency.EUR, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most %d".formatted(MAX_SCALE));
        }

        @Test
        void acceptsExtraTrailingZeros() {
            LedgerLine ledgerLine = line(Currency.EUR, "10.50000");
            assertThat(ledgerLine.amount().scale()).isEqualTo(MAX_SCALE);
            assertThat(ledgerLine.amount()).isEqualTo(new BigDecimal("10.5000"));
        }

        @Test
        void setsScaleToMaxScale() {
            LedgerLine ledgerLine = line(Currency.EUR, "10.8");
            assertThat(ledgerLine.amount().scale()).isEqualTo(MAX_SCALE);
            assertThat(ledgerLine.amount()).isEqualTo(new BigDecimal("10.8000"));
        }

        @Test
        void treatsSameValueWithDifferentScaleAsEqual() {
            UUID accountId = UUID.randomUUID();
            LedgerLine line1 = line(accountId, Currency.EUR, "10.8");
            LedgerLine line2 = line(accountId, Currency.EUR, "10.80");
            assertThat(line1).isEqualTo(line2);
        }

        @Test
        void acceptsNegativeScaleThatFits() {
            LedgerLine ledgerLine = line(Currency.EUR, "1E+3"); // 1 × 10³ = 1000
            assertThat(ledgerLine.amount()).isEqualTo(new BigDecimal("1000.0000"));
        }

        @Test
        void rejectsExtremeNegativeScale() {
            BigDecimal amount = new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE);
            assertThatThrownBy(() -> new LedgerLine(UUID.randomUUID(), Currency.EUR, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most %d".formatted(MAX_DIGITS));
        }

        @Test
        void rejectsExtremePositiveScale() {
            BigDecimal amount = new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE);
            assertThatThrownBy(() -> new LedgerLine(UUID.randomUUID(), Currency.EUR, amount))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most %d".formatted(MAX_SCALE));
        }

        @Test
        void acceptsNegativeAmountAtLimits() {
            LedgerLine ledgerLine = line(Currency.EUR, "-999999999999999.9999");
            assertThat(ledgerLine.amount()).isEqualTo(new BigDecimal("-999999999999999.9999"));
        }

        @Test
        void rejectsNegativeAmountOverMaxDigits() {
            assertThatThrownBy(() -> line(Currency.EUR, "-" + "1".repeat(MAX_DIGITS + 1)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at most %d".formatted(MAX_DIGITS));
        }
    }
}
