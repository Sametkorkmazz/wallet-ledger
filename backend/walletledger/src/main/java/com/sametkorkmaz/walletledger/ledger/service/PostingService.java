package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.AccountNotFoundException;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerRuleException;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import com.sametkorkmaz.walletledger.ledger.persistence.AccountStore;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
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
        Map<AccountId, Account> accounts = getAccounts(accountIds);
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

    @Transactional
    public EntryId createTransfer(UserId userId, AccountId from, AccountId to, String amount, String description) {
        Map<AccountId, Account> accounts = getAccounts(List.of(from, to));
        if (!accounts.get(from).userId().equals(userId)) {
            throw new AccountNotFoundException(from);
        }
        Money transferAmount = Money.of(accounts.get(from).currency(), amount);
        if (transferAmount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LedgerRuleException("Transfer amount must be positive");
        }

        LedgerLine credit = new LedgerLine(from, transferAmount.negate());
        LedgerLine debit = new LedgerLine(to, transferAmount);

        LedgerEntry entry = LedgerEntry.of(description, EntryType.TRANSFER, userId, List.of(credit, debit));
        return ledgerStore.saveEntry(entry);
    }

    private Map<AccountId, Account> getAccounts(Collection<AccountId> ids) {
        Map<AccountId, Account> accounts = accountStore.findByIds(ids);
        Set<AccountId> missingIds = new HashSet<>(ids);
        missingIds.removeAll(accounts.keySet());
        if (!missingIds.isEmpty()) {
            throw new AccountNotFoundException(missingIds);
        }
        return accounts;
    }
}
