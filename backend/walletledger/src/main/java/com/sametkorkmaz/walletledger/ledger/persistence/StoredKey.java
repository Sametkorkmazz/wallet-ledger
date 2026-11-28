package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;

public record StoredKey(String requestHash, EntryId entryId, int status, String body) {
}
