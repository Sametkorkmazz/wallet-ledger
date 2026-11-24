package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.AccountNotFoundException;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, AccountStore.class})
@DataJpaTest
class AccountStoreTest {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountStore accountStore;

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private AccountEntity saveAccount(UUID userId, Currency currency) {
        return accountRepository.saveAndFlush(new AccountEntity(userId, currency));
    }

    @Nested
    @DisplayName("Find by ids")
    class FindByIds {
        @Test
        void returnsEmptyMapForNoIds() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<AccountId, Account> found = accountStore.findByIds(List.of());
            assertThat(found).isEmpty();
        }

        @Test
        void returnsAccountsForKnownIds() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            AccountEntity accountEntity1 = saveAccount(userId, Currency.EUR);
            AccountEntity accountEntity2 = saveAccount(userId, Currency.USD);
            testEntityManager.clear();

            Map<AccountId, Account> found = accountStore.findByIds(List.of(AccountId.of(accountEntity1.getId()), AccountId.of(accountEntity2.getId())));
            assertThat(found).containsOnlyKeys(AccountId.of(accountEntity1.getId()), AccountId.of(accountEntity2.getId()));
            Account account1 = found.get(AccountId.of(accountEntity1.getId()));
            assertThat(account1.id().value()).isEqualTo(accountEntity1.getId());
            assertThat(account1.currency()).isEqualTo(Currency.EUR);
            assertThat(account1.userId().value()).isEqualTo(userId);

            Account account2 = found.get(AccountId.of(accountEntity2.getId()));
            assertThat(account2.id().value()).isEqualTo(accountEntity2.getId());
            assertThat(account2.currency()).isEqualTo(Currency.USD);
            assertThat(account2.userId().value()).isEqualTo(userId);
        }

        @Test
        void returnsOnlyRequestedAccounts() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            AccountEntity accountEntity1 = saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<AccountId, Account> found = accountStore.findByIds(List.of(AccountId.of(accountEntity1.getId())));
            assertThat(found).containsOnlyKeys(AccountId.of(accountEntity1.getId()));
        }

        @Test
        void omitsUnknownIdsFromResult() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            AccountEntity accountEntity = saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<AccountId, Account> found = accountStore.findByIds(List.of(AccountId.of(accountEntity.getId()), AccountId.of(UUID.randomUUID())));
            assertThat(found).containsOnlyKeys(AccountId.of(accountEntity.getId()));
        }

        @Test
        void returnsEmptyMapForUnknownIds() {
            UUID userId = TestData.insertUser(jdbcTemplate);
            saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<AccountId, Account> found = accountStore.findByIds(List.of(AccountId.of(UUID.randomUUID())));
            assertThat(found).isEmpty();
        }

        @Test
        void returnsExternalAccountWithNullUserId() {
            AccountEntity accountEntity = saveAccount(null, Currency.EUR);
            testEntityManager.clear();
            Map<AccountId, Account> found = accountStore.findByIds(List.of(AccountId.of(accountEntity.getId())));
            assertThat(found).containsOnlyKeys(AccountId.of(accountEntity.getId()));
            assertThat(found.get(AccountId.of(accountEntity.getId())).userId()).isNull();
        }
    }

    @Nested
    @DisplayName("Get by id or throw")
    class GetByIdOrThrow {
        @Test
        void getsKnownAccount() {
            AccountEntity accountEntity = saveAccount(null, Currency.EUR);
            testEntityManager.clear();
            Account account = accountStore.getByIdOrThrow(AccountId.of(accountEntity.getId()));
            assertThat(account.id()).isEqualTo(AccountId.of(accountEntity.getId()));
            assertThat(account.userId()).isNull();
            assertThat(account.currency()).isEqualTo(Currency.EUR);
        }

        @Test
        void throwsUnknownAccount() {
            saveAccount(null, Currency.EUR);
            testEntityManager.clear();
            UUID unknownId = UUID.randomUUID();
            assertThatThrownBy(() -> accountStore.getByIdOrThrow(AccountId.of(unknownId)))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Account not found with id: " + unknownId);
        }
    }
}
