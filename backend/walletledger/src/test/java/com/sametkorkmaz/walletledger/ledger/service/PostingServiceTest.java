package com.sametkorkmaz.walletledger.ledger.service;


import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.AccountNotFoundException;
import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.EntryType;
import com.sametkorkmaz.walletledger.ledger.domain.ErrorCode;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerEntry;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerLine;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerRuleException;
import com.sametkorkmaz.walletledger.ledger.domain.Money;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import com.sametkorkmaz.walletledger.ledger.persistence.AccountStore;
import com.sametkorkmaz.walletledger.ledger.persistence.IdempotencyStore;
import com.sametkorkmaz.walletledger.ledger.persistence.LedgerStore;
import com.sametkorkmaz.walletledger.ledger.persistence.StoredKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PostingServiceTest {

    private static final UUID IDEMPOTENCY_KEY = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UserId OWNER = UserId.of(UUID.randomUUID());
    private static final Account FROM = new Account(AccountId.of(UUID.randomUUID()), OWNER, Currency.EUR);
    private static final Account TO = new Account(AccountId.of(UUID.randomUUID()), OWNER, Currency.EUR);
    private static final EntryId ENTRY_ID = EntryId.of(UUID.randomUUID());
    private static final Money AMOUNT = Money.of(Currency.EUR, "10");

    @Mock
    private AccountStore accountStore;
    @Mock
    private LedgerStore ledgerStore;
    @Mock
    private IdempotencyStore idempotencyStore;
    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder().build();
    @InjectMocks
    private PostingService postingService;

    private static LineRequest createLineRequest(AccountId accountId, BigDecimal amount) {
        return new LineRequest(accountId, amount);
    }

    private static List<LineRequest> createLineRequests(Account from, Account to, String amount) {
        LineRequest debitLine = createLineRequest(from.id(), new BigDecimal(amount).negate());
        LineRequest creditLine = createLineRequest(to.id(), new BigDecimal(amount));
        return List.of(debitLine, creditLine);
    }

    private static Account createAccount(Currency currency) {
        return new Account(AccountId.of(UUID.randomUUID()), UserId.of(UUID.randomUUID()), currency);
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
            given(ledgerStore.saveEntry(any(LedgerEntry.class))).willReturn(EntryId.of(UUID.randomUUID()));
            postingService.postLedger("TEST", EntryType.TRANSFER, lines);
            ArgumentCaptor<LedgerEntry> ledgerEntryArgumentCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
            then(ledgerStore).should().saveEntry(ledgerEntryArgumentCaptor.capture());
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
            given(ledgerStore.saveEntry(any(LedgerEntry.class))).willReturn(EntryId.of(UUID.randomUUID()));
            postingService.postLedger("TEST", EntryType.TOP_UP, lines);
            ArgumentCaptor<LedgerEntry> ledgerEntryArgumentCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
            then(ledgerStore).should().saveEntry(ledgerEntryArgumentCaptor.capture());
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
            given(ledgerStore.saveEntry(any(LedgerEntry.class))).willReturn(EntryId.of(savedEntryId));
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
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
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
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
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
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void rejectsEmptyLines() {
            given(accountStore.findByIds(Collections.emptySet())).willReturn(Collections.emptyMap());
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, Collections.emptyList()))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry must have at least 2 lines");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void rejectsSingleLine() {
            Account account1 = createAccount(Currency.EUR);
            LineRequest lineRequest1 = createLineRequest(account1.id(), new BigDecimal("10.00"));
            given(accountStore.findByIds(Set.of(lineRequest1.accountId())))
                    .willReturn(Map.of(account1.id(), account1));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, List.of(lineRequest1)))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry must have at least 2 lines");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
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
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("EUR allows at most 2 decimal places");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
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
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry does not balance for");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsBalancedWithDifferentCurrencies() {
            Account account1 = createAccount(Currency.EUR);
            Account account2 = createAccount(Currency.USD);
            List<LineRequest> lines = createLineRequests(account2, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id(), account2.id())))
                    .willReturn(Map.of(account1.id(), account1, account2.id(), account2));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry does not balance for");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsDuplicateAccount() {
            Account account1 = createAccount(Currency.EUR);
            List<LineRequest> lines = createLineRequests(account1, account1, "10");
            given(accountStore.findByIds(Set.of(account1.id())))
                    .willReturn(Map.of(account1.id(), account1));
            assertThatThrownBy(() -> postingService.postLedger("TEST", EntryType.TRANSFER, lines))
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("Entry must not contain duplicate accounts");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
        }

        @Test
        void throwsNonDomainExceptionUnwrapped() {
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
                    .isInstanceOf(LedgerRuleException.class)
                    .hasMessageContaining("reversesEntryId is required for REVERSAL and not allowed for other types");
            then(ledgerStore).should(never()).saveEntry(any(LedgerEntry.class));
        }
    }

    @Nested
    @DisplayName("Create transfer")
    class CreateTransfer {
        @Test
        void savesTransferEntryWhenRequestIsValidAndKeyIsNew() {
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(FROM.id(), FROM, TO.id(), TO));
            given(idempotencyStore.tryInsert(any(), any(), any())).willReturn(true);
            given(ledgerStore.saveEntry(any())).willReturn(ENTRY_ID);

            ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);

            TransferResult result = postingService.createTransfer(OWNER, IDEMPOTENCY_KEY, FROM.id(), TO.id(), "10.00", "rent");

            then(ledgerStore).should().saveEntry(captor.capture());
            assertThat(captor.getValue()).isEqualTo(LedgerEntry.transfer(OWNER, FROM, TO, Money.of(Currency.EUR, "10.00"), "rent"));
            assertThat(result.entryId()).isEqualTo(ENTRY_ID);
            assertThat(result.replayed()).isFalse();

            then(idempotencyStore).should().tryInsert(OWNER, IDEMPOTENCY_KEY, TransferFingerprint.hashRequest(FROM.id(), TO.id(), AMOUNT, "rent"));
            then(idempotencyStore).should().complete(eq(OWNER), eq(IDEMPOTENCY_KEY), eq(ENTRY_ID), eq(201), anyString());
        }


        @Test
        void replaysStoredEntryWhenKeyExistsWithSameHash() {
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(FROM.id(), FROM, TO.id(), TO));
            given(idempotencyStore.tryInsert(any(), any(), any())).willReturn(false);
            String requestHash = TransferFingerprint.hashRequest(FROM.id(), TO.id(), AMOUNT, "rent");
            given(idempotencyStore.find(any(), any())).willReturn(Optional.of(new StoredKey(requestHash, ENTRY_ID, HttpStatus.CREATED.value(), "entryId: " + ENTRY_ID)));

            TransferResult result = postingService.createTransfer(OWNER, IDEMPOTENCY_KEY, FROM.id(), TO.id(), "10.00", "rent");

            then(idempotencyStore).should().tryInsert(OWNER, IDEMPOTENCY_KEY, requestHash);
            then(idempotencyStore).should().find(OWNER, IDEMPOTENCY_KEY);
            then(idempotencyStore).should(never()).complete(any(), any(), any(), anyInt(), any());
            then(ledgerStore).should(never()).saveEntry(any());

            assertThat(result).isEqualTo(new TransferResult(ENTRY_ID, true));
        }

        @Test
        void throwsKeyReusedWhenKeyExistsWithDifferentHash() {
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(FROM.id(), FROM, TO.id(), TO));
            given(idempotencyStore.tryInsert(any(), any(), any())).willReturn(false);
            given(idempotencyStore.find(any(), any())).willReturn(
                    Optional.of(new StoredKey(TransferFingerprint.hashRequest(FROM.id(), TO.id(), Money.of(Currency.USD, "15.00"), "rent"),
                            ENTRY_ID, HttpStatus.CREATED.value(), "entryId: " + ENTRY_ID)));

            assertThatThrownBy(() -> postingService.createTransfer(OWNER, IDEMPOTENCY_KEY, FROM.id(), TO.id(), "10.00", "rent"))
                    .isInstanceOfSatisfying(IdempotencyKeyReusedException.class, e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.IDEMPOTENCY_KEY_REUSED));

            then(idempotencyStore).should().tryInsert(OWNER, IDEMPOTENCY_KEY, TransferFingerprint.hashRequest(FROM.id(), TO.id(), AMOUNT, "rent"));
            then(idempotencyStore).should(never()).complete(any(), any(), any(), anyInt(), any());
            then(ledgerStore).should(never()).saveEntry(any());
        }

        @Test
        void throwsAccountNotFoundWhenToAccountDoesNotExist() {
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(FROM.id(), FROM));
            assertThatThrownBy(() -> postingService.createTransfer(OWNER, IDEMPOTENCY_KEY, FROM.id(), TO.id(), "10.00", "rent"))
                    .isInstanceOfSatisfying(AccountNotFoundException.class,
                            e -> assertThat(e.getCode()).isEqualTo("LEDGER-404-001"));
            then(idempotencyStore).shouldHaveNoInteractions();
            then(ledgerStore).should(never()).saveEntry(any());
        }

        @Test
        void throwsDuplicateAccountWhenFromEqualsTo() {
            given(accountStore.findByIds(anyCollection())).willReturn(Map.of(FROM.id(), FROM));
            given(idempotencyStore.tryInsert(any(), any(), any())).willReturn(true);
            assertThatThrownBy(() -> postingService.createTransfer(OWNER, IDEMPOTENCY_KEY, FROM.id(), FROM.id(), "10.00", "rent"))
                    .isInstanceOfSatisfying(LedgerRuleException.class,
                            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.LEDGER_RULE_VIOLATED));
            then(ledgerStore).should(never()).saveEntry(any());
        }
    }
}
