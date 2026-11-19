package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import com.sametkorkmaz.walletledger.ledger.persistence.AccountStore;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostingService {

    private final LedgerStore ledgerStore;
    private final AccountStore accountStore;

    @Transactional
    public UUID postLedger(String description, EntryType entryType, List<LineRequest> lineRequests) {
        Set<AccountId> accountIds = lineRequests.stream().map(LineRequest::accountId).collect(Collectors.toSet());
        Map<AccountId, Account> accounts = accountStore.findByIds(accountIds);
        Set<AccountId> missingIds = new HashSet<>(accountIds);
        missingIds.removeAll(accounts.keySet());
        if (!missingIds.isEmpty()) {
            throw new AccountNotFoundException(missingIds);
        }
        List<LedgerLine> lines = new ArrayList<>();
        for (LineRequest lineRequest : lineRequests) {
            LedgerLine line = new LedgerLine(lineRequest.accountId(),
                    new Money(accounts.get(lineRequest.accountId()).currency(), lineRequest.amount()));
            lines.add(line);
        }
        LedgerEntry entry = LedgerEntry.of(description, entryType, null, lines);
        return ledgerStore.saveEntry(entry).value();
    }
}
