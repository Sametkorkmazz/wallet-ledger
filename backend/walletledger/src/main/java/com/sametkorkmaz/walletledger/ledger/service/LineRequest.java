package com.sametkorkmaz.walletledger.ledger.service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record LineRequest(UUID accountId, BigDecimal amount) {

    public LineRequest {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }
}
