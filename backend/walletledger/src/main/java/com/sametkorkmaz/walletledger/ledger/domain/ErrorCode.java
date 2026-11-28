package com.sametkorkmaz.walletledger.ledger.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    LEDGER_RULE_VIOLATED("LEDGER-422-000", "Ledger rule violated"),
    TRANSFER_CURRENCY_MISMATCH("LEDGER-422-011", "Currencies differ"),
    TRANSFER_AMOUNT_NOT_POSITIVE("LEDGER-422-012", "Amount must be positive"),
    IDEMPOTENCY_KEY_REUSED("LEDGER-422-020", "Idempotency key reused with a different request");

    private final String code;
    private final String title;
}
