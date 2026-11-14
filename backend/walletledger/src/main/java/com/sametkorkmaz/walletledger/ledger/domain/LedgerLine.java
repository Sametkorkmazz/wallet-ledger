package com.sametkorkmaz.walletledger.ledger.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * One posting to a single account within a ledger entry.
 * <p>
 * The amount is signed and used to check that the entry balances per currency.
 * A positive amount increases the account's balance, a negative amount decreases it.
 * The currency comes from {@code amount.currency()} and scale/digits are guaranteed by {@code Money}.
 *
 * @param accountId the account this line posts to
 * @param amount    signed, non-zero amount; its currency is the account's currency (scale and digits are checked by Money)
 */
public record LedgerLine(UUID accountId, Money amount) {
    public LedgerLine {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        if (amount.isZero()) {
            throw new IllegalArgumentException("amount must not be zero");
        }
    }
}
