package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    private UUID insertUser() {
        return jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash) values (?,'test123') RETURNING ID",
                UUID.class, UUID.randomUUID() + "@test.com");
    }

    private AccountEntity saveAccount(UUID userId, Currency currency) {
        return accountRepository.saveAndFlush(new AccountEntity(userId, currency));
    }

    @Nested
    @DisplayName("Find by ids")
    class FindByIds {
        @Test
        void returnsEmptyMapForNoIds() {
            UUID userId = insertUser();
            saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<UUID, Account> found = accountStore.findByIds(List.of());
            assertThat(found).isEmpty();
        }

        @Test
        void returnsAccountsForKnownIds() {
            UUID userId = insertUser();
            AccountEntity accountEntity1 = saveAccount(userId, Currency.EUR);
            AccountEntity accountEntity2 = saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<UUID, Account> found = accountStore.findByIds(List.of(accountEntity1.getId(), accountEntity2.getId()));
            assertThat(found).containsOnlyKeys(accountEntity1.getId(), accountEntity2.getId());
            Account account1 = found.get(accountEntity1.getId());
            assertThat(account1.id()).isEqualTo(accountEntity1.getId());
            assertThat(account1.currency()).isEqualTo(Currency.EUR);
            assertThat(account1.userId()).isEqualTo(userId);

            Account account2 = found.get(accountEntity2.getId());
            assertThat(account2.id()).isEqualTo(accountEntity2.getId());
            assertThat(account2.currency()).isEqualTo(Currency.USD);
            assertThat(account2.userId()).isEqualTo(userId);
        }

        @Test
        void returnsOnlyRequestedAccounts() {
            UUID userId = insertUser();
            AccountEntity accountEntity1 = saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<UUID, Account> found = accountStore.findByIds(List.of(accountEntity1.getId()));
            assertThat(found).containsOnlyKeys(accountEntity1.getId());
        }

        @Test
        void omitsUnknownIdsFromResult() {
            UUID userId = insertUser();
            AccountEntity accountEntity = saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<UUID, Account> found = accountStore.findByIds(List.of(accountEntity.getId(), UUID.randomUUID()));
            assertThat(found).containsOnlyKeys(accountEntity.getId());
        }

        @Test
        void returnsEmptyMapForUnknownIds() {
            UUID userId = insertUser();
            saveAccount(userId, Currency.EUR);
            saveAccount(userId, Currency.USD);
            testEntityManager.clear();
            Map<UUID, Account> found = accountStore.findByIds(List.of(UUID.randomUUID()));
            assertThat(found).isEmpty();
        }

        @Test
        void returnsExternalAccountWithNullUserId() {
            AccountEntity accountEntity = saveAccount(null, Currency.EUR);
            testEntityManager.clear();
            Map<UUID, Account> found = accountStore.findByIds(List.of(accountEntity.getId()));
            assertThat(found).containsOnlyKeys(accountEntity.getId());
            assertThat(found.get(accountEntity.getId()).userId()).isNull();
        }
    }
}
