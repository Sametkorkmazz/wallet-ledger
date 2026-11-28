package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.AccountNotFoundException;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import com.sametkorkmaz.walletledger.ledger.persistence.AccountStore;
import com.sametkorkmaz.walletledger.ledger.persistence.IdempotencyStore;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import com.sametkorkmaz.walletledger.ledger.persistence.StoredKey;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
    private final IdempotencyStore idempotencyStore;
    private final ObjectMapper objectMapper;

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
    public TransferResult createTransfer(UserId userId, UUID idempotencyKey, AccountId from, AccountId to, String amount, String description) {
        Map<AccountId, Account> accounts = getAccounts(List.of(from, to));
        Money transferAmount = Money.of(accounts.get(from).currency(), amount);
        String requestHash = TransferFingerprint.hashRequest(from, to, transferAmount, description);
        boolean isNewKey = idempotencyStore.tryInsert(userId, idempotencyKey, requestHash);
        if (!isNewKey) {
            StoredKey storedKey = idempotencyStore.find(userId, idempotencyKey).orElseThrow(IllegalStateException::new);
            if (!storedKey.requestHash().equals(requestHash)) {
                throw new IdempotencyKeyReusedException("Using same idempotency key for a different request is not allowed");
            }
            return new TransferResult(storedKey.entryId(), true);
        }
        LedgerEntry entry = LedgerEntry.transfer(userId, accounts.get(from), accounts.get(to), transferAmount, description);
        EntryId entryId = ledgerStore.saveEntry(entry);
        idempotencyStore.complete(userId, idempotencyKey, entryId, HttpStatus.CREATED.value(), objectMapper.writeValueAsString(Map.of("entryId", entryId.value())));
        return new TransferResult(entryId, false);
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
