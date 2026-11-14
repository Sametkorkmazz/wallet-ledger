package com.sametkorkmaz.walletledger.ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An amount of money in one currency.
 * <p>
 * The amount is normalized to the currency's minor units (2 for EUR, USD and TRY), so {@code 10.5} and {@code 10.50}
 * are equal. Amounts with more decimals than the currency allows are rejected, never rounded (ADR 4). Zero and
 * negative amounts are allowed: whether a zero line or a negative balance is valid is decided by the caller.
 * Arithmetic is only allowed between amounts of the same currency.
 *
 * @param currency the currency; its minor units set the scale
 * @param amount   signed amount; at most {@value #MAX_DIGITS} integer digits and at most the currency's minor units
 *                 as decimals (throws IllegalArgumentException otherwise)
 */
public record Money(Currency currency, BigDecimal amount) {
    public static final int MAX_DIGITS = 15;

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        long digits = (long) amount.precision() - amount.scale();
        if (digits > MAX_DIGITS) {
            throw new IllegalArgumentException("amount must have at most %d integer digits (currently %d)".formatted(MAX_DIGITS, digits));
        }
        try {
            amount = amount.setScale(currency.minorUnits(), RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("%s allows at most %d decimal places".formatted(currency, currency.minorUnits()), e);
        }
    }

    /**
     * Parses {@code amount} as a decimal string, e.g. {@code Money.of(Currency.EUR, "10.50")}.
     */
    public static Money of(Currency currency, String amount) {
        return new Money(currency, new BigDecimal(amount));
    }

    /**
     * Returns the sum. Throws IllegalArgumentException if the currencies differ or the result has too many digits.
     */
    public Money add(Money money) {
        checkSameCurrency(money);
        BigDecimal total = amount.add(money.amount());
        return new Money(currency, total);
    }

    /**
     * Returns the difference. Throws IllegalArgumentException if the currencies differ or the result has too many digits.
     */
    public Money subtract(Money money) {
        checkSameCurrency(money);
        BigDecimal total = amount.subtract(money.amount());
        return new Money(currency, total);
    }

    public Money negate() {
        return new Money(currency, amount.negate());
    }

    public boolean isZero() {
        return BigDecimal.ZERO.compareTo(amount) == 0;
    }

    private void checkSameCurrency(Money money) {
        if (currency != money.currency()) {
            throw new IllegalArgumentException("cannot combine %s with %s".formatted(currency, money.currency()));
        }
    }
}
