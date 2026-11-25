package com.sametkorkmaz.walletledger.ledger.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class LedgerRuleException extends RuntimeException {
    private final ErrorCode errorCode;

    public LedgerRuleException(String message) {
        super(message);
        this.errorCode = ErrorCode.LEDGER_RULE_VIOLATED;
    }

    public LedgerRuleException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
    }

    public LedgerRuleException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.LEDGER_RULE_VIOLATED;
    }

}
