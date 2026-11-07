package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "journal_lines")
@Getter
public class JournalLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(name = "amount", nullable = false, precision = LedgerLine.MAX_DIGITS + LedgerLine.MAX_SCALE, scale = LedgerLine.MAX_SCALE)
    private BigDecimal amount;

    protected JournalLineEntity() {
    }

    public JournalLineEntity(UUID accountId, UUID entryId, BigDecimal amount) {
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(entryId, "entryId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        this.accountId = accountId;
        this.entryId = entryId;
        try {
            this.amount = amount.setScale(LedgerLine.MAX_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("amount must have at most %d decimal places".formatted(LedgerLine.MAX_SCALE), e);
        }
    }
}
