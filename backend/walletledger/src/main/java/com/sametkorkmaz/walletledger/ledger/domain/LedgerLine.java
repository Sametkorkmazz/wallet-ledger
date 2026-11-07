package com.sametkorkmaz.walletledger.ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * One posting to a single account within a ledger entry.
 * <p>
 * The amount is signed and used to check that the entry balances per currency.
 * Amounts have at most 4 decimals and 15 integer digits and are normalized to scale 4.
 * A positive amount increases the account's balance, a negative amount decreases it.
 * The currency is the account's currency.
 *
 * @param accountId the account this line posts to
 * @param currency  the account's currency, used to check that entry balances per currency
 * @param amount    signed, non-zero amount; at most 4 decimals and 15 integer digits, stored at scale 4 (throws
 *                  IllegalArgumentException otherwise)
 */
public record LedgerLine(UUID accountId, Currency currency, BigDecimal amount) {
    public static final int MAX_SCALE = 4;
    public static final int MAX_DIGITS = 15;

    public LedgerLine {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("amount must not be zero");
        }
        long digits = (long) amount.precision() - amount.scale();
        if (digits > MAX_DIGITS) {
            throw new IllegalArgumentException("amount must have at most %d integer digits (currently %d)".formatted(MAX_DIGITS, digits));
        }
        try {
            amount = amount.setScale(MAX_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("amount must have at most %d decimal places".formatted(MAX_SCALE), e);
        }
    }
}
