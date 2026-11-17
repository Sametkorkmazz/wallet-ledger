package com.sametkorkmaz.walletledger.ledger.domain;

public class LedgerRuleException extends RuntimeException {
    public LedgerRuleException(String message) {
        super(message);
    }

    public LedgerRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
