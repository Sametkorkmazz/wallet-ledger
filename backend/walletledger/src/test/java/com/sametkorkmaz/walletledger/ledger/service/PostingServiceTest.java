package com.sametkorkmaz.walletledger.ledger.service;


import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostingServiceTest {

    @Mock
    private AccountStore accountStore;

    @Mock
    private LedgerStore ledgerStore;

    @InjectMocks
    private PostingService postingService;

    private static LineRequest createLineRequest(UUID accountId, BigDecimal amount) {
        return new LineRequest(accountId, amount);
    }

    private static List<LineRequest> createLineRequests(Account from, Account to, String amount) {
        LineRequest debitLine = createLineRequest(from.id(), new BigDecimal(amount).negate());
        LineRequest creditLine = createLineRequest(to.id(), new BigDecimal(amount));
        return List.of(debitLine, creditLine);
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
            List<LineRequest> lines = Stream.concat(createLineRequests(account2, account1, "10").stream(),
                    createLineRequests(account3, account4, "30").stream()).toList();
            given(accountStore.findByIds(Set.of(account1.id(), account2.id(), account3.id(), account4.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2, account3.id(), account3, account4.id(), account4));
            postingService.postLedger("TEST", EntryType.TRANSFER, lines);
            ArgumentCaptor<LedgerEntry> ledgerEntryArgumentCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
            verify(ledgerStore).saveEntry(ledgerEntryArgumentCaptor.capture());
            LedgerEntry saved = ledgerEntryArgumentCaptor.getValue();
            assertThat(saved.lines()).extracting(LedgerLine::accountId, l -> l.amount().currency())
                    .containsExactlyInAnyOrder(tuple(account1.id(), account1.currency()), tuple(account2.id(), account2.currency()),
                            tuple(account3.id(), account3.currency()), tuple(account4.id(), account4.currency()));
        }

        @Test
        void entryTypeComesFromService() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            postingService.postLedger("TEST", EntryType.TOP_UP, lines);
            ArgumentCaptor<LedgerEntry> ledgerEntryArgumentCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
            verify(ledgerStore).saveEntry(ledgerEntryArgumentCaptor.capture());
            LedgerEntry saved = ledgerEntryArgumentCaptor.getValue();
            assertThat(saved.entryType()).isEqualTo(EntryType.TOP_UP);
        }

        @Test
        void returnsSavedEntryId() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            UUID savedEntryId = UUID.randomUUID();
            given(ledgerStore.saveEntry(any(LedgerEntry.class))).willReturn(savedEntryId);
            UUID response = postingService.postLedger("TEST", EntryType.TRANSFER, lines);
            assertThat(response).isEqualTo(savedEntryId);
        }

        @Test
        void throwsNullDescription() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger(null, EntryType.TRANSFER, lines))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("description must not be null");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsNullEntryType() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", null, lines))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("entryType must not be null");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsUnknownAccounts() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            Account unknown1 = createAccount(Currency.EUR);
            Account unknown2 = createAccount(Currency.EUR);
            List<LineRequest> lines = Stream.concat(createLineRequests(account2, account1, "10").stream(),
                    createLineRequests(unknown2, unknown1, "20").stream()).toList();
            given(accountStore.findByIds(Set.of(account1.id(), account2.id(), unknown1.id(), unknown2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, lines))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Accounts not found").hasMessageContainingAll(unknown1.id().toString(), unknown2.id().toString())
                    .hasMessageNotContainingAny(account1.id().toString(), account2.id().toString());
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void rejectsEmptyLines() {
            given(accountStore.findByIds(Collections.emptySet())).willReturn(Collections.emptyMap());
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, Collections.emptyList()))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry must have at least 2 lines");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void rejectsSingleLine() {
            Account account1 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10.00"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId())))
                    .willReturn(Map.of(account1.id(), account1));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, List.of(lineRequest1)))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry must have at least 2 lines");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsOverMaxAllowedDecimals() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10.001"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-10.00"));
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("EUR allows at most 2 decimal places");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsUnbalancedLines() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10"));
            LineRequest lineRequest2 = createLineRequest(account2.id(), new BigDecimal("-120"));
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, List.of(lineRequest1, lineRequest2)))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry does not balance for");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsBalancedWithDifferentCurrencies() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.USD);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, lines))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry does not balance for");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsDuplicateAccount() {
            Account account1 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account1, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id())))
                    .willReturn(Map.of(account1.id(), account1));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, lines))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("Entry must not contain duplicate accounts");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsLedgerSaveExceptionUnwrapped() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(anyCollection()))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            given(ledgerStore.saveEntry(any())).willThrow(new IllegalArgumentException("bug"));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, lines))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("bug");
        }

        @Test
        void rejectsReversalEntry() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(anyCollection()))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.REVERSAL, lines))
                    .isInstanceOf(InvalidPostingException.class)
                    .hasMessageContaining("reversesEntryId is required for REVERSAL and not allowed for other types");
            verify(ledgerStore, never()).saveEntry(any(LedgerEntry.class));
        }
    }
}
