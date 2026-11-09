package com.sametkorkmaz.walletledger.ledger.service;


import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.persistence.AccountStore;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostingServiceTest {

    @Mock
    private AccountStore accountStore;

    @Mock
    private LedgerStore ledgerStore;

    @InjectMocks
    private PostingService postingService;

    private static LineRequest createLineRequest(UUID accountId, BigDecimal amount) {
        return new LineRequest(accountId, amount);
    }

    private static Account createAccount(Currency currency) {
        return new Account(UUID.randomUUID(), UUID.randomUUID(), currency);
    }

    @Nested
    @DisplayName("Post ledger")
    class PostLedger {
        @Test
        void currencyComesFromAccount() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            Account account3 = createAccount(Currency.USD);
            Account account4 = createAccount(Currency.USD);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10"));
            LineRequest lineRequest3 = createLineRequest(account3.id(), new BigDecimal("-30"));
            LineRequest lineRequest4 = createLineRequest(account4.id(), new BigDecimal("30"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId(), lineRequest2.accountId(), lineRequest3.accountId(), lineRequest4.accountId())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2, account3.id(), account3, account4.id(), account4));
            postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2, lineRequest3, lineRequest4));
            ArgumentCaptor<LedgerEntry> ledgerEntryArgumentCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
            verify(ledgerStore).saveEntry(ledgerEntryArgumentCaptor.capture());
            LedgerEntry saved = ledgerEntryArgumentCaptor.getValue();
            assertThat(saved.lines()).extracting(LedgerLine::accountId, LedgerLine::currency)
                    .containsExactlyInAnyOrder(tuple(account1.id(), account1.currency()), tuple(account2.id(), account2.currency()),
                            tuple(account3.id(), account3.currency()), tuple(account4.id(), account4.currency()));
        }

        @Test
        void returnsSavedEntryId() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10"));
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            UUID savedEntryId = UUID.randomUUID();
            given(ledgerStore.saveEntry(any(LedgerEntry.class))).willReturn(savedEntryId);
            UUID response = postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2));
            assertThat(response).isEqualTo(savedEntryId);
        }

        @Test
        void throwsNullDescription() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId(), lineRequest2.accountId())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger(null, List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("description must not be null");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsUnknownAccounts() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10"));
            LineRequest lineRequest3 = createLineRequest(UUID.randomUUID(), new BigDecimal("20"));
            LineRequest lineRequest4 = createLineRequest(UUID.randomUUID(), new BigDecimal("-20"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId(), lineRequest2.accountId(), lineRequest3.accountId(), lineRequest4.accountId())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2, lineRequest3, lineRequest4)))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Accounts not found").hasMessageContainingAll(lineRequest3.accountId().toString(), lineRequest4.accountId().toString())
                    .hasMessageNotContainingAny(account1.id().toString(), account2.id().toString());
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsUnbalancedLines() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-120"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId(), lineRequest2.accountId())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry does not balance for");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsBalancedWithDifferentCurrencies() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.USD);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId(), lineRequest2.accountId())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry does not balance for");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsDuplicateAccount() {
            Account account1 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account1.id(), new BigDecimal("-10"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId())))
                    .willReturn(Map.of(account1.id(), account1));
            assertThatThrownBy(() -> postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry must not contain duplicate accounts");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsLedgerSaveExceptionUnwrapped() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10"));
            given(accountStore.findByIds(anyCollection()))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            given(ledgerStore.saveEntry(any())).willThrow(new IllegalArgumentException("bug"));
            assertThatThrownBy(() -> postingService.postLedger("TEST", List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("bug");
        }
    }
}
