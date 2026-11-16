package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@Import(TestcontainersConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JournalEntryRepositoryTest {
    @Autowired
    JournalEntryRepository journalEntryRepository;
    @Autowired
    TestEntityManager testEntityManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static JournalEntryEntity createEntry(String description) {
        return new JournalEntryEntity(description, EntryType.TRANSFER, null, null);
    }

    @Nested
    @DisplayName("Saving and reading")
    class SavingAndReading {
        @Test
        void savesJournalEntry() {
            JournalEntryEntity saved = journalEntryRepository.saveAndFlush(createEntry("TEST"));
            testEntityManager.clear();
            JournalEntryEntity found = journalEntryRepository.findById(saved.getId()).orElseThrow();
            assertThat(found.getDescription()).isEqualTo("TEST");
            assertThat(found.getCreatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
            assertThat(found.getEntryType()).isEqualTo(EntryType.TRANSFER);
            assertThat(found.getReversesEntryId()).isNull();
            assertThat(found.getCreatedBy()).isNull();
        }

        @Test
        void savesReversalWithLinkAndCreator() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            JournalEntryEntity original = journalEntryRepository.saveAndFlush(createEntry("TEST"));
            JournalEntryEntity reversal = journalEntryRepository.saveAndFlush(
                    new JournalEntryEntity("REVERSAL", EntryType.REVERSAL, original.getId(), userId));
            testEntityManager.clear();
            JournalEntryEntity found = journalEntryRepository.findById(reversal.getId()).orElseThrow();
            assertThat(found.getEntryType()).isEqualTo(EntryType.REVERSAL);
            assertThat(found.getReversesEntryId()).isEqualTo(original.getId());
            assertThat(found.getCreatedBy()).isEqualTo(userId);
        }
    }
}
