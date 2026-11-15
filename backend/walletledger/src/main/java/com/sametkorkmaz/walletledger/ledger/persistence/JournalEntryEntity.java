package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
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

    JournalEntryEntity(String description, EntryType entryType, UUID reversesEntryId, UUID createdBy) {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(entryType, "entryType must not be null");
        this.description = description;
        this.entryType = entryType;
        this.reversesEntryId = reversesEntryId;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }
}
