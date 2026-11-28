package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.ErrorCode;
import lombok.Getter;

@Getter
public class IdempotencyKeyReusedException extends RuntimeException {
    private final ErrorCode errorCode;

    public IdempotencyKeyReusedException(String message) {
        super(message);
        this.errorCode = ErrorCode.IDEMPOTENCY_KEY_REUSED;
    }
}
