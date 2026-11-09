package com.sametkorkmaz.walletledger.ledger.service;

public class InvalidPostingException extends RuntimeException {
    public InvalidPostingException(String message) {
        super(message);
    }
}
