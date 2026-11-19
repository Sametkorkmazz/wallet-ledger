package com.sametkorkmaz.walletledger.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountIdTest {

    @Nested
    @DisplayName("Value")
    class Value {
        @Test
        void rejectsNullValue() {
            assertThatThrownBy(() -> new AccountId(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("value must not be null");
        }

        @Test
        void keepsValue() {
            UUID value = UUID.randomUUID();
            AccountId accountId = new AccountId(value);
            assertThat(accountId.value()).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("To String")
    class ToString {
        @Test
        void returnsValueAsString() {
            UUID value = UUID.randomUUID();
            AccountId accountId = new AccountId(value);
            assertThat(accountId.toString()).isEqualTo(value.toString());
        }
    }
}
