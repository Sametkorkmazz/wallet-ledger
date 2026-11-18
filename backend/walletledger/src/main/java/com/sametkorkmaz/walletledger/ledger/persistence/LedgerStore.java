package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerStore {
    private final JournalEntryRepository journalEntryRepository;
    private final JournalLineRepository journalLineRepository;

    /**
     * Currency is not stored: the caller built the lines from loaded accounts
     */
    @Transactional
    public EntryId saveEntry(LedgerEntry ledgerEntry) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity(ledgerEntry.description(), ledgerEntry.entryType(),
                valueOrNull(ledgerEntry.reversesEntryId()), valueOrNull(ledgerEntry.createdBy()));
        journalEntryEntity = journalEntryRepository.saveAndFlush(journalEntryEntity);
        List<JournalLineEntity> lines = new ArrayList<>();
        for (LedgerLine line : ledgerEntry.lines()) {
            lines.add(new JournalLineEntity(line.accountId().value(), journalEntryEntity.getId(), line.amount().amount()));
        }
        journalLineRepository.saveAllAndFlush(lines);
        return EntryId.of(journalEntryEntity.getId());
    }

    private UUID valueOrNull(EntryId entryId) {
        return entryId == null ? null : entryId.value();
    }

    private UUID valueOrNull(UserId userId) {
        return userId == null ? null : userId.value();
    }

    public BigDecimal balanceOf(UUID accountId) {
        return journalLineRepository.sumAmountByAccountId(accountId);
    }
}
