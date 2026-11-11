package com.sametkorkmaz.walletledger.ledger.service;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Collection<UUID> ids) {
        super("Accounts not found with ids: %s".formatted(ids.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))));
    }
}
