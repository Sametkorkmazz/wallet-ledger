package com.sametkorkmaz.walletledger.ledger.service;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TransferFingerprintTest {
    private static final AccountId FROM = AccountId.of(UUID.randomUUID());
    private static final AccountId TO = AccountId.of(UUID.randomUUID());
    private static final Money AMOUNT = Money.of(Currency.EUR, "10.00");

    @Nested
    @DisplayName("Hash request")
    class HashRequest {
        public static Stream<Arguments> differentFields() {
            return Stream.of(
                    Arguments.of(AccountId.of(UUID.randomUUID()), TO, AMOUNT, "rent"),
                    Arguments.of(FROM, AccountId.of(UUID.randomUUID()), AMOUNT, "rent"),
                    Arguments.of(FROM, TO, AMOUNT, "education")
            );
        }

        @Test
        void sameInputGivesSameHash() {
            String firstHash = TransferFingerprint.hashRequest(FROM, TO, AMOUNT, "rent");
            String secondHash = TransferFingerprint.hashRequest(FROM, TO, AMOUNT, "rent");
            assertThat(firstHash).isEqualTo(secondHash);
        }

        @Test
        void sameCurrencyWithDifferentDecimalsGiveSameHash() {
            String firstHash = TransferFingerprint.hashRequest(FROM, TO, Money.of(Currency.EUR, "10.0"), "rent");
            String secondHash = TransferFingerprint.hashRequest(FROM, TO, Money.of(Currency.EUR, "10.00"), "rent");
            assertThat(firstHash).isEqualTo(secondHash);
        }

        @Test
        void differentCurrencyGiveDifferentHash() {
            String firstHash = TransferFingerprint.hashRequest(FROM, TO, Money.of(Currency.USD, "10.00"), "rent");
            String secondHash = TransferFingerprint.hashRequest(FROM, TO, Money.of(Currency.EUR, "10.00"), "rent");
            assertThat(firstHash).isNotEqualTo(secondHash);
        }

        @Test
        void differentAmountGiveDifferentHash() {
            String firstHash = TransferFingerprint.hashRequest(FROM, TO, Money.of(Currency.EUR, "15.00"), "rent");
            String secondHash = TransferFingerprint.hashRequest(FROM, TO, Money.of(Currency.EUR, "10.00"), "rent");
            assertThat(firstHash).isNotEqualTo(secondHash);
        }

        @ParameterizedTest
        @MethodSource("differentFields")
        void changingEachFieldGivesDifferentHash(AccountId fromId, AccountId toId, Money amount, String description) {
            String firstHash = TransferFingerprint.hashRequest(FROM, TO, AMOUNT, "rent");
            String secondHash = TransferFingerprint.hashRequest(fromId, toId, amount, description);
            assertThat(firstHash).isNotEqualTo(secondHash);
        }

        @Test
        void returns64LowercaseHexCharacters() {
            String hash = TransferFingerprint.hashRequest(FROM, TO, AMOUNT, "rent");
            assertThat(hash).matches("[0-9a-f]{64}");
        }
    }
}
