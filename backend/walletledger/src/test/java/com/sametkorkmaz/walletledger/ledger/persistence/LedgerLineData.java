package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;

import java.util.UUID;

public class LedgerLineData {
    public static final AccountId ACCOUNT_ID = AccountId.of(UUID.randomUUID());
    public static final EntryId ENTRY_ID = EntryId.of(UUID.randomUUID());

    public static JournalLineEntity create() {
        return create(ENTRY_ID, ACCOUNT_ID, "10");
    }

    public static JournalLineEntity create(String amount) {
        return create(ENTRY_ID, ACCOUNT_ID, amount);
    }

    public static JournalLineEntity create(EntryId entryId, AccountId accountId, String amount) {
        return new JournalLineEntity(entryId, new LedgerLine(accountId, Money.of(Currency.EUR, amount)));
    }
}
