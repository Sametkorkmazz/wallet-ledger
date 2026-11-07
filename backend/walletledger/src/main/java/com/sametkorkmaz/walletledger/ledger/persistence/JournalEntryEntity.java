package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "journal_entries")
@Getter
public class JournalEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "description", length = LedgerEntry.MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    protected JournalEntryEntity() {
    }

    public JournalEntryEntity(String description) {
        Objects.requireNonNull(description, "description must not be null");
        this.description = description;
        this.createdAt = Instant.now();
    }
}
