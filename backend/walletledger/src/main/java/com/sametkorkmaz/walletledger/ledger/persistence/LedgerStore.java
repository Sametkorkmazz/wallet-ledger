package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Saves journal entries and reads balances.
 * <p>
 * Must be called inside a service transaction (05-transactions rule 2): this class opens none, so a call
 * outside one commits each statement on its own.
 */
@Component
@RequiredArgsConstructor
public class LedgerStore {
    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;

    /**
     * Currency is not stored: the caller built the lines from loaded accounts
     */
    public EntryId saveEntry(LedgerEntry ledgerEntry) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity(ledgerEntry);
        journalEntryEntity = journalEntryRepository.saveAndFlush(journalEntryEntity);
        List<JournalLineEntity> lines = new ArrayList<>();
        EntryId savedId = EntryId.of(journalEntryEntity.getId());
        for (LedgerLine line : ledgerEntry.lines()) {
            lines.add(new JournalLineEntity(savedId, line));
        }
        journalLineRepository.saveAllAndFlush(lines);
        return savedId;
    }

    public Money balanceOf(Account account) {
        return new Money(account.currency(), journalLineRepository.sumAmountByAccountId(account.id().value()));
    }
}
