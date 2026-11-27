package com.sametkorkmaz.walletledger.ledger.domain;

import com.sametkorkmaz.walletledger.shared.domain.NotFoundException;

import java.util.Collection;
import java.util.stream.Collectors;

public class AccountNotFoundException extends NotFoundException {
    public AccountNotFoundException(AccountId id) {
        super("LEDGER-404-001", "Account not found with id: %s".formatted(id));
    }

    public AccountNotFoundException(Collection<AccountId> ids) {
        super("LEDGER-404-001", "Accounts not found with ids: %s".formatted(ids.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))));
    }
}
