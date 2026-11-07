package com.sametkorkmaz.walletledger.ledger.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalEntryEntityTest {

    private static final String DESCRIPTION = "TEST";

    private static JournalEntryEntity createEntry() {
        return new JournalEntryEntity(DESCRIPTION);
    }

    @Nested
    @DisplayName("Valid construction")
    class ValidConstruction {
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

    @Nested
    @DisplayName("Null arguments")
    class NullConstraints {
        @Test
        void rejectsNullDescription() {
            assertThatThrownBy(() -> new JournalEntryEntity(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("description must not be null");
        }
    }
}
