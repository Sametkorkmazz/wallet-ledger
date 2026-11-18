package com.sametkorkmaz.walletledger.ledger.domain;

import java.util.Objects;
import java.util.UUID;

public record EntryId(UUID value) {
    public EntryId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static EntryId of(UUID value) {
        return new EntryId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
