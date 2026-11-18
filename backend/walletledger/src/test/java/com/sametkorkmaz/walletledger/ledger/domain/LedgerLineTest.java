package com.sametkorkmaz.walletledger.ledger.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerLineTest {
    private LedgerLine line(Currency c, String amount) {
        return new LedgerLine(AccountId.of(UUID.randomUUID()), Money.of(c, amount));
    }

    private LedgerLine line(UUID accountId, Currency c, String amount) {
        return new LedgerLine(AccountId.of(accountId), Money.of(c, amount));
    }

    @Nested
    @DisplayName("Account")
    class Account {
        @Test
        void rejectsNullAccountId() {
            assertThatThrownBy(() -> new LedgerLine(null, new Money(Currency.EUR, BigDecimal.TEN)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("accountId must not be null");
        }
    }

    @Nested
    @DisplayName("Amount")
    class Amount {
        @Test
        void rejectsNullAmount() {
            assertThatThrownBy(() -> new LedgerLine(AccountId.of(UUID.randomUUID()), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("amount must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-0.00", "0.00"})
        void rejectsZeroAmount(String amount) {
            assertThatThrownBy(() -> line(Currency.EUR, amount))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("amount must not be zero");
        }

        @Test
        void treatsSameValueWithDifferentScaleAsEqual() {
            UUID accountId = UUID.randomUUID();
            LedgerLine line1 = line(accountId, Currency.EUR, "10.8");
            LedgerLine line2 = line(accountId, Currency.EUR, "10.80");
            assertThat(line1).isEqualTo(line2);
        }
    }
}
