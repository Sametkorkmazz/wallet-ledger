package com.sametkorkmaz.walletledger.ledger.domain;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A balanced set of ledger lines that will be posted together.
 * <p>
 * Rules (checked on construction, so an invalid entry cannot exist):
 * at least two lines, no zero amounts, and the amounts sum to zero per currency.
 * A positive amount increases an account's balance, a negative amount decreases it.
 * The currency comes from the account and is passed in by the posting service.
 *
 * @param description human-readable reason for the entry, e.g. "top-up" (not blank).
 * @param lines       at least two lines; amounts must sum to zero per currency
 */
public record LedgerEntry(String description, List<LedgerLine> lines) {

    public LedgerEntry {
        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be empty");
        }
        Objects.requireNonNull(lines, "lines must not be null");

        lines = List.copyOf(lines);
        if (lines.size() < 2) {
            throw new IllegalArgumentException("Entry must have at least 2 lines");
        }

        EnumMap<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (LedgerLine line : lines) {
            if (line.amount().compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("Line amount must not be zero (account %s)".formatted(line.accountId()));
            }
            totals.merge(line.currency(), line.amount(), BigDecimal::add);
        }

        for (Map.Entry<Currency, BigDecimal> currencyTotal : totals.entrySet()) {
            if (currencyTotal.getValue().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Entry does not balance for " + currencyTotal.getKey() + ": total = " + currencyTotal.getValue());
            }
        }
    }
}
