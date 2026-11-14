package com.sametkorkmaz.walletledger.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.sametkorkmaz.walletledger.ledger.domain.Currency.EUR;
import static com.sametkorkmaz.walletledger.ledger.domain.Currency.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Nested
    @DisplayName("Currency")
    class CurrencyField {
        @Test
        void rejectsNullCurrency() {
            assertThatThrownBy(() -> Money.of(null, "10.50"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currency must not be null");
        }
    }

    @Nested
    @DisplayName("Amount")
    class Amount {
        @Test
        void rejectsNullAmount() {
            assertThatThrownBy(() -> new Money(EUR, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("amount must not be null");
        }

        @Test
        void rejectsMoreDecimalsThanMinorUnits() {
            assertThatThrownBy(() -> Money.of(EUR, "10.005"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EUR allows at most 2 decimal");
        }

        @Test
        void normalizesScaleToMinorUnits() {
            Money money = Money.of(EUR, "10.5");
            assertThat(money.amount().scale()).isEqualTo(2);
            assertThat(Money.of(EUR, "10.50")).isEqualTo(money);
        }

        @Test
        void acceptsZerosAfterCent() {
            Money money = Money.of(EUR, "10.5000");
            assertThat(money.amount().scale()).isEqualTo(2);
            assertThat(Money.of(EUR, "10.50")).isEqualTo(money);
        }

        @Test
        void acceptsZero() {
            Money money = Money.of(EUR, "0");
            assertThat(money.isZero()).isTrue();
        }

        @Test
        void acceptsNegative() {
            Money money = Money.of(EUR, "-10.50");
            assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("-10.50"));
        }

        @Test
        void acceptsMaxDigits() {
            Money money = Money.of(EUR, "1E+14");
            assertThat(money.amount()).isEqualByComparingTo("100000000000000");   // 15 digits
        }

        @Test
        void rejectsOverMaxDigits() {
            assertThatThrownBy(() -> Money.of(EUR, "1E+15"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must have at most 15 integer digits");
        }

        @Timeout(1)
        @Test
        void rejectsExtremeNegativeScale() {
            assertThatThrownBy(() -> new Money(EUR, new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must have at most 15 integer digits");
        }

        @Timeout(1)
        @Test
        void rejectsExtremePositiveScale() {
            assertThatThrownBy(() -> new Money(EUR, new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("EUR allows at most 2 decimal places");
        }
    }

    @Nested
    @DisplayName("Arithmetic")
    class Arithmetic {
        @Test
        void add() {
            Money money1 = Money.of(EUR, "10.50");
            Money money2 = Money.of(EUR, "2.25");
            Money total = money1.add(money2);
            assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("12.75"));
        }

        @Test
        void allowsNegativeResult() {
            Money money1 = Money.of(EUR, "10.50");
            Money money2 = Money.of(EUR, "-12.75");
            Money total = money1.add(money2);
            assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("-2.25"));
        }

        @Test
        void subtract() {
            Money money1 = Money.of(EUR, "10.50");
            Money money2 = Money.of(EUR, "2.25");
            Money total = money1.subtract(money2);
            assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("8.25"));
        }

        @Test
        void rejectsAddingDifferentCurrency() {
            Money money1 = Money.of(EUR, "10.50");
            Money money2 = Money.of(USD, "2.25");
            assertThatThrownBy(() -> money1.add(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot combine EUR with USD");
        }

        @Test
        void rejectsSubtractingDifferentCurrency() {
            Money money1 = Money.of(EUR, "10.50");
            Money money2 = Money.of(USD, "2.25");
            assertThatThrownBy(() -> money1.subtract(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot combine EUR with USD");
        }

        @Test
        void sumOfTwo15DigitOverflows() {
            Money money1 = Money.of(EUR, "9E+14");
            Money money2 = Money.of(EUR, "2E+14");
            assertThatThrownBy(() -> money1.add(money2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("amount must have at most 15 integer digits");
        }

        @Test
        void negate() {
            Money money = Money.of(EUR, "10.50");
            assertThat(money.negate()).isEqualTo(Money.of(EUR, "-10.50"));
        }

        @Test
        void negateTwiceEqualsOriginal() {
            Money money = Money.of(EUR, "10.50");
            assertThat(money.negate().negate()).isEqualTo(money);
        }

        @Test
        void addAndSubtractEqualsOriginal() {
            Money money1 = Money.of(EUR, "10.50");
            Money money2 = Money.of(EUR, "2.50");
            assertThat(money1.add(money2).subtract(money2)).isEqualTo(money1);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "0.00", "-0"})
        void isZeroForZero(String amount) {
            assertThat(Money.of(EUR, amount).isZero()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"0.01", "-0.01"})
        void isNotZeroForSmallestAmounts(String amount) {
            assertThat(Money.of(EUR, amount).isZero()).isFalse();
        }

        @Test
        void subtractingItselfIsZero() {
            Money money = Money.of(EUR, "10.50");
            assertThat(money.subtract(money).isZero()).isTrue();
        }
    }
}
