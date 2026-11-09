package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.persistence.AccountStore;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostingService {

    private final LedgerStore ledgerStore;
    private final AccountStore accountStore;

    @Transactional
    public UUID postLedger(String description, List<LineRequest> lineRequests) {
        Set<UUID> accountIds = lineRequests.stream().map(LineRequest::accountId).collect(Collectors.toSet());
        Map<UUID, Account> accounts = accountStore.findByIds(accountIds);
        Set<UUID> missingIds = new HashSet<>(accountIds);
        missingIds.removeAll(accounts.keySet());
        if (!missingIds.isEmpty()) {
            throw new AccountNotFoundException(missingIds);
        }
        List<LedgerLine> lines = new ArrayList<>();
        LedgerEntry entry;
        try {
            for (LineRequest lineRequest : lineRequests) {
                LedgerLine line = new LedgerLine(lineRequest.accountId(), accounts.get(lineRequest.accountId()).currency(), lineRequest.amount());
                lines.add(line);
            }
            entry = new LedgerEntry(description, lines);
        } catch (IllegalArgumentException e) {
            throw new InvalidPostingException(e.getMessage());
        }
        return ledgerStore.saveEntry(entry);
    }
}
