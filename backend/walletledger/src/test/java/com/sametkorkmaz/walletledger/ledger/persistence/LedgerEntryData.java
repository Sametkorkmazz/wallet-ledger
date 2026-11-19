package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;

import java.util.List;
import java.util.UUID;

class LedgerEntryData {
    public static final String DESCRIPTION = "TEST";

    public static JournalEntryEntity createEntry(String description, EntryType entryType, EntryId reversesEntryId,
                                                 UserId createdBy) {
        return new JournalEntryEntity(new LedgerEntry(description, entryType, reversesEntryId, createdBy,
                List.of(new LedgerLine(AccountId.of(UUID.randomUUID()), Money.of(Currency.EUR, "10")),
                        new LedgerLine(AccountId.of(UUID.randomUUID()), Money.of(Currency.EUR, "-10"))
                )));
    }

    public static JournalEntryEntity createEntry() {
        return createEntry(DESCRIPTION, EntryType.TRANSFER, null, null);
    }
}
