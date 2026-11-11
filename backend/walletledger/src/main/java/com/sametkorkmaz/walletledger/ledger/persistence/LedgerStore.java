package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
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
    public UUID saveEntry(LedgerEntry ledgerEntry) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity(ledgerEntry.description());
        journalEntryEntity = journalEntryRepository.saveAndFlush(journalEntryEntity);
        List<JournalLineEntity> lines = new ArrayList<>();
        for (LedgerLine line : ledgerEntry.lines()) {
            lines.add(new JournalLineEntity(line.accountId(), journalEntryEntity.getId(), line.amount()));
        }
        journalLineRepository.saveAllAndFlush(lines);
        return journalEntryEntity.getId();
    }

    public BigDecimal balanceOf(UUID accountId) {
        return journalLineRepository.sumAmountByAccountId(accountId);
    }
}
