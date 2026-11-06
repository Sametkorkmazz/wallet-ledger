package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.TestcontainersConfiguration;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Import(TestcontainersConfiguration.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TestEntityManager entityManager;
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static AccountEntity anAccount(Currency currency) {
        return new AccountEntity(null, currency);
    }

    private static AccountEntity anAccount(UUID userId, Currency currency) {
        return new AccountEntity(userId, currency);
    }

    @Nested
    @DisplayName("Save an account")
    class SaveAccount {
        @Test
        void savesAccount() {
            AccountEntity account = accountRepository.saveAndFlush(anAccount(Currency.EUR));
            entityManager.clear();
            Optional<AccountEntity> saved = accountRepository.findById(account.getId());
            assertThat(saved).isPresent();
            assertThat(saved.get().getUserId()).isNull();
            assertThat(saved.get().getCurrency()).isEqualTo(Currency.EUR);
            assertThat(saved.get().getCreatedAt()).isNotNull();
        }

        @Test
        void rejectsSecondExternalAccountInSameCurrency() {
            accountRepository.saveAndFlush(anAccount(Currency.EUR));
            assertThatThrownBy(() -> accountRepository.saveAndFlush(anAccount(Currency.EUR)))
                    .isInstanceOf(DataIntegrityViolationException.class)
                    .hasMessageContaining("uq_accounts_external_currency");
        }

        @Test
        void acceptsSameCurrencyForAnExternalAccountAndUserAccount() {
            UUID userId = jdbcTemplate.queryForObject("INSERT INTO users (email, password_hash) values ('test@gmail.com','test123') RETURNING id",
                    UUID.class);
            AccountEntity account = accountRepository.saveAndFlush(anAccount(userId, Currency.USD));
            entityManager.clear();
            AccountEntity external = assertDoesNotThrow(() -> accountRepository.saveAndFlush(anAccount(Currency.USD)));
            entityManager.clear();

            Optional<AccountEntity> userSaved = accountRepository.findById(account.getId());
            assertThat(userSaved).isPresent();
            assertThat(userSaved.get().getUserId()).isEqualTo(userId);
            assertThat(userSaved.get().getCurrency()).isEqualTo(Currency.USD);
            assertThat(userSaved.get().getCreatedAt()).isNotNull();

            Optional<AccountEntity> externalSaved = accountRepository.findById(external.getId());
            assertThat(externalSaved).isPresent();
            assertThat(externalSaved.get().getUserId()).isNull();
            assertThat(externalSaved.get().getCurrency()).isEqualTo(Currency.USD);
            assertThat(externalSaved.get().getCreatedAt()).isNotNull();
        }
    }
}
