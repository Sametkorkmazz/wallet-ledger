package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.Money;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class TransferFingerprint {
    private static final String OPERATION = "transfer:v1";

    private TransferFingerprint() {
    }

    public static String hashRequest(AccountId fromId, AccountId toId, Money amount, String description) {
        Objects.requireNonNull(fromId, "fromId must not be null");
        Objects.requireNonNull(toId, "toId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(description, "description must not be null");
        StringBuilder sb = new StringBuilder();
        append(sb, OPERATION);
        append(sb, fromId.value().toString());
        append(sb, toId.value().toString());
        append(sb, amount.currency().name());
        append(sb, amount.amount().toPlainString());
        append(sb, description);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
        byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    private static void append(StringBuilder sb, String value) {
        sb.append(value.length()).append("#").append(value);
    }
}
