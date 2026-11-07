package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AccountEntityTest {

    @Nested
    @DisplayName("Create entity")
    class CreateAccountEntity {
        @Test
        void createsEntity() {
            UUID userId = UUID.randomUUID();
            AccountEntity accountEntity = new AccountEntity(userId, Currency.EUR);
            assertThat(accountEntity.getUserId()).isEqualTo(userId);
            assertThat(accountEntity.getCurrency()).isEqualTo(Currency.EUR);
            assertThat(accountEntity.getCreatedAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
        }

        @Test
        void rejectsNullCurrency() {
            assertThatThrownBy(() -> new AccountEntity(UUID.randomUUID(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currency must not be null");
        }
    }
}
