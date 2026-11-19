package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;

import java.math.BigDecimal;
import java.util.Objects;

public record LineRequest(AccountId accountId, BigDecimal amount) {

    public LineRequest {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
    }
}
