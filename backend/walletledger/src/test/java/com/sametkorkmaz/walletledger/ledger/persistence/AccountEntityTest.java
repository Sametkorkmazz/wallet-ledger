package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        }

        @Test
        void rejectsNullCurrency() {
            assertThatThrownBy(() -> new AccountEntity(UUID.randomUUID(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("currency must not be null");
        }
    }
}
