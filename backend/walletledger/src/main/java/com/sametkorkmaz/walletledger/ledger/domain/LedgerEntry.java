package com.sametkorkmaz.walletledger.ledger.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A balanced set of ledger lines that will be posted together.
 * <p>
 * Rules (checked on construction, so an invalid entry cannot exist):
 * at least two lines, no duplicate account lines, and the amounts sum to zero per currency.
 * A positive amount increases an account's balance, a negative amount decreases it.
 * The currency comes from the account and is passed in by the posting service.
 *
 * @param description     human-readable reason for the entry, e.g. "top-up".
 *                        Leading and trailing whitespace is trimmed; must not be blank and at most 255 characters.
 * @param entryType       why the entry exists (TRANSFER, TOP_UP, REVERSAL, SYSTEM); must not be null
 * @param reversesEntryId the entry this one reverses; required for REVERSAL, must be null otherwise
 * @param createdBy       the user who started the entry; null for system entries
 * @param lines           at least two lines; amounts must sum to zero per currency
 */
public record LedgerEntry(String description, EntryType entryType, UUID reversesEntryId, UUID createdBy,
                          List<LedgerLine> lines) {

    public static final int MAX_DESCRIPTION_LENGTH = 255;

    public LedgerEntry {
        Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new LedgerRuleException("description must not be blank");
        }
        String trimmedDesc = description.trim();
        if (trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new LedgerRuleException("description must be at most %d characters".formatted(MAX_DESCRIPTION_LENGTH));
        }
        description = trimmedDesc;

        Objects.requireNonNull(entryType, "entryType must not be null");
        boolean isReversal = entryType == EntryType.REVERSAL;
        boolean hasLink = reversesEntryId != null;
        if (isReversal != hasLink) {
            throw new LedgerRuleException("reversesEntryId is required for REVERSAL and not allowed for other types");
        }

        Objects.requireNonNull(lines, "lines must not be null");
        for (LedgerLine ledgerLine : lines) {
            Objects.requireNonNull(ledgerLine, "line must not be null");
        }
        lines = List.copyOf(lines);
        if (lines.size() < 2) {
            throw new LedgerRuleException("Entry must have at least 2 lines");
        }
        if (lines.stream().map(LedgerLine::accountId).distinct().count() != lines.size()) {
            throw new LedgerRuleException("Entry must not contain duplicate accounts");
        }
        EnumMap<Currency, Money> totals = new EnumMap<>(Currency.class);
        for (LedgerLine line : lines) {
            totals.merge(line.amount().currency(), line.amount(), Money::add);
        }
        for (Map.Entry<Currency, Money> currencyTotal : totals.entrySet()) {
            if (!currencyTotal.getValue().isZero()) {
                throw new LedgerRuleException("Entry does not balance for " + currencyTotal.getKey() + ": total = " + currencyTotal.getValue().amount());
            }
        }
    }

    public static LedgerEntry of(String description, EntryType entryType, UUID createdBy, List<LedgerLine> lines) {
        return new LedgerEntry(description, entryType, null, createdBy, lines);
    }
}
