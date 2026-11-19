package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "journal_lines")
@Getter
class JournalLineEntity {

    static final int AMOUNT_SCALE = 4;
    static final int AMOUNT_PRECISION = 19;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;

    @Column(name = "amount", nullable = false, precision = AMOUNT_PRECISION, scale = AMOUNT_SCALE)
    private BigDecimal amount;

    protected JournalLineEntity() {
    }

    JournalLineEntity(EntryId entryId, LedgerLine ledgerLine) {
        this.accountId = ledgerLine.accountId().value();
        this.entryId = entryId.value();
        this.amount = ledgerLine.amount().amount().setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }
}
