package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;

import java.util.Objects;

public record TransferResult(EntryId entryId, boolean replayed) {
    public TransferResult {
        Objects.requireNonNull(entryId, "entryId must not be null");
    }
}
