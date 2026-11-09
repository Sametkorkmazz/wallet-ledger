package com.sametkorkmaz.walletledger.ledger.service;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record LineRequest(
        @NotNull UUID accountId,
        @NotNull BigDecimal amount) {

    public LineRequest {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }
}
