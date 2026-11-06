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
 * at least two lines, no duplicate account lines, and the amounts sum to zero per currency.
 * A positive amount increases an account's balance, a negative amount decreases it.
 * The currency comes from the account and is passed in by the posting service.
 *
 * @param description human-readable reason for the entry, e.g. "top-up".
 *                    Leading and trailing whitespace is trimmed; must not be blank and at most 255 characters.
 * @param lines       at least two lines; amounts must sum to zero per currency
 */
public record LedgerEntry(String description, List<LedgerLine> lines) {

    private static final int MAX_DESCRIPTION_LENGTH = 255;

    public LedgerEntry {
        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        String trimmedDesc = description.trim();
        if (trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("description must be at most %d characters".formatted(MAX_DESCRIPTION_LENGTH));
        }
        description = trimmedDesc;

        Objects.requireNonNull(lines, "lines must not be null");
        for (LedgerLine ledgerLine : lines) {
            Objects.requireNonNull(ledgerLine, "line must not be null");
        }
        lines = List.copyOf(lines);
        if (lines.size() < 2) {
            throw new IllegalArgumentException("Entry must have at least 2 lines");
        }
        if (lines.stream().map(LedgerLine::accountId).distinct().count() != lines.size()) {
            throw new IllegalArgumentException("Entry must not contain duplicate accounts");
        }
        EnumMap<Currency, BigDecimal> totals = new EnumMap<>(Currency.class);
        for (LedgerLine line : lines) {
            totals.merge(line.currency(), line.amount(), BigDecimal::add);
        }
        for (Map.Entry<Currency, BigDecimal> currencyTotal : totals.entrySet()) {
            if (currencyTotal.getValue().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Entry does not balance for " + currencyTotal.getKey() + ": total = " + currencyTotal.getValue());
            }
        }
    }
}
