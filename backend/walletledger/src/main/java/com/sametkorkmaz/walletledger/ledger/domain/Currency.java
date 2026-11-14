package com.sametkorkmaz.walletledger.ledger.domain;

/**
 * Supported currencies; minor units are the number of decimals the currency allows (ISO 4217).
 */
public enum Currency {
    EUR(2),
    USD(2),
    TRY(2);

    private final int minorUnits;

    Currency(int minorUnits) {
        this.minorUnits = minorUnits;
    }

    public int minorUnits() {
        return minorUnits;
    }
}
