package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "journal_entries")
@Getter
class JournalEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "description", length = LedgerEntry.MAX_DESCRIPTION_LENGTH)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 20, nullable = false)
    private EntryType entryType;

    @Column(name = "reverses_entry_id")
    private UUID reversesEntryId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected JournalEntryEntity() {
    }

    JournalEntryEntity(LedgerEntry ledgerEntry) {
        this.description = ledgerEntry.description();
        this.entryType = ledgerEntry.entryType();
        this.reversesEntryId = valueOrNull(ledgerEntry.reversesEntryId());
        this.createdBy = valueOrNull(ledgerEntry.createdBy());
        this.createdAt = Instant.now();
    }

    private static UUID valueOrNull(UserId userId) {
        return userId == null ? null : userId.value();
    }

    private static UUID valueOrNull(EntryId entryId) {
        return entryId == null ? null : entryId.value();
    }
}
