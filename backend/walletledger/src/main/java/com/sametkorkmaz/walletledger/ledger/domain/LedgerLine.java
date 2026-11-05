package com.sametkorkmaz.walletledger.ledger.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * One posting to a single account within a ledger entry.
 * <p>
 * Amount is signed number. A positive amount increases the account's balance, a negative amount decreases it.
 * The currency is the account's currency.
 *
 * @param accountId the account this line posts to
 * @param currency  the account's currency, used to check that entry balances per currency
 * @param amount    signed, non-zero amount (the zero check happens in {@code LedgerEntry}
 */
public record LedgerLine(UUID accountId, Currency currency, BigDecimal amount) {
    public LedgerLine {
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
    }
}
