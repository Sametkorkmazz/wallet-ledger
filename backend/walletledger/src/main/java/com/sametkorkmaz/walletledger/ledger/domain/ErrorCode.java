package com.sametkorkmaz.walletledger.ledger.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    LEDGER_RULE_VIOLATED("LEDGER-422-000", "Ledger rule violated");

    private final String code;
    private final String title;
}
