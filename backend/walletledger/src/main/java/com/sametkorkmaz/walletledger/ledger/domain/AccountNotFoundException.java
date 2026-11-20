package com.sametkorkmaz.walletledger.ledger.domain;

import java.util.Collection;
import java.util.stream.Collectors;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId id) {
        super("Account not found with id: %s".formatted(id));
    }

    public AccountNotFoundException(Collection<AccountId> ids) {
        super("Accounts not found with ids: %s".formatted(ids.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))));
    }
}
