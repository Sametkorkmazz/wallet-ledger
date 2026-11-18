package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;

import java.util.Collection;
import java.util.stream.Collectors;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Collection<AccountId> ids) {
        super("Accounts not found with ids: %s".formatted(ids.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))));
    }
}
