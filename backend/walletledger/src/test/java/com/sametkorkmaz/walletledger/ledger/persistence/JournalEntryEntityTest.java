package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static com.sametkorkmaz.walletledger.ledger.persistence.LedgerEntryData.DESCRIPTION;
import static com.sametkorkmaz.walletledger.ledger.persistence.LedgerEntryData.createEntry;
import static org.assertj.core.api.Assertions.assertThat;

class JournalEntryEntityTest {

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {
        @Test
        void keepsDescriptionAndEntryType() {
            JournalEntryEntity entryEntity = createEntry();
            assertThat(entryEntity.getDescription()).isEqualTo(DESCRIPTION);
            assertThat(entryEntity.getEntryType()).isEqualTo(EntryType.TRANSFER);
        }

        @Test
        void keepsReversesEntryIdAndCreatedBy() {
            UUID reversesEntryId = UUID.randomUUID();
            UUID createdBy = UUID.randomUUID();
            JournalEntryEntity entryEntity = createEntry("TEST", EntryType.REVERSAL, EntryId.of(reversesEntryId), UserId.of(createdBy));
            assertThat(entryEntity.getReversesEntryId()).isEqualTo(reversesEntryId);
            assertThat(entryEntity.getCreatedBy()).isEqualTo(createdBy);
        }

        @Test
        void keepsDescriptionAndCreatedAt() {
            JournalEntryEntity entryEntity = createEntry();
            assertThat(entryEntity.getDescription()).isEqualTo(DESCRIPTION);
        }

        @Test
        void setsCreatedAtToCreationTime() {
            Instant before = Instant.now();
            JournalEntryEntity entryEntity = createEntry();
            Instant after = Instant.now();
            assertThat(entryEntity.getCreatedAt()).isBetween(before, after);
        }
    }

}
