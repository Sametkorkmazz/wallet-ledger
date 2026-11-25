package com.sametkorkmaz.walletledger.ledger.api;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.AccountNotFoundException;
import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.LedgerRuleException;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import com.sametkorkmaz.walletledger.ledger.service.PostingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FROM = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TO = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ENTRY = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PostingService postingService;

    private static String readRequest(String name) throws IOException {
        return new ClassPathResource("requests/" + name).getContentAsString(StandardCharsets.UTF_8);
    }

    private MvcTestResult postTransfer(String userId, String body) {
        return mvc.post().uri("/api/v1/transfers")
                .header("X-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .exchange();
    }

    @Nested
    @DisplayName("POST /transfers")
    class PostTransfers {

        @Test
        void returns201WithLocationAndEntryIdWhenRequestIsValid() throws IOException {
            given(postingService.createTransfer(any(), any(), any(), any(), any())).willReturn(EntryId.of(ENTRY));

            MvcTestResult result = postTransfer(USER.toString(), readRequest("create-transfer.json"));

            assertThat(result).hasStatus(HttpStatus.CREATED);
            assertThat(result).headers().hasValue("Location", "http://localhost/api/v1/transfers/" + ENTRY);
            assertThat(result).bodyJson().extractingPath("$.entryId").isEqualTo(ENTRY.toString());
        }

        @Test
        void passesRequestValuesToServiceWhenRequestIsValid() throws IOException {
            given(postingService.createTransfer(any(), any(), any(), any(), any())).willReturn(EntryId.of(ENTRY));

            postTransfer(USER.toString(), readRequest("create-transfer.json"));

            then(postingService).should()
                    .createTransfer(UserId.of(USER), AccountId.of(FROM), AccountId.of(TO), "10.00", "Rent");
        }

        @Test
        void returns400WhenAmountIsMissing() throws IOException {
            MvcTestResult result = postTransfer(USER.toString(), readRequest("create-transfer-missing-amount.json"));

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
            then(postingService).shouldHaveNoInteractions();
        }

        @Test
        void returns400WhenDescriptionIsBlank() throws IOException {
            MvcTestResult result = postTransfer(USER.toString(), readRequest("create-transfer-blank-description.json"));

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void returns400WhenUserIdIsNotUuid() throws IOException {
            MvcTestResult result = postTransfer("abc", readRequest("create-transfer.json"));

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void returns400WhenUserIdIsMissing() throws IOException {
            MvcTestResult result = mvc.post().uri("/api/v1/transfers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(readRequest("create-transfer.json"))
                    .exchange();

            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        }

        @Test
        void returns422ProblemWithErrorCodeWhenLedgerRuleIsBroken() throws IOException {
            given(postingService.createTransfer(any(), any(), any(), any(), any()))
                    .willThrow(new LedgerRuleException("Entry does not balance for EUR"));

            MvcTestResult result = postTransfer(USER.toString(), readRequest("create-transfer.json"));

            assertThat(result).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
            assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(result).bodyJson().extractingPath("$.detail").isEqualTo("Entry does not balance for EUR");
            assertThat(result).bodyJson().extractingPath("$.errorCode").isEqualTo("LEDGER-422-000");
        }

        @Test
        void returns404ProblemWithErrorCodeWhenAccountIsUnknown() throws IOException {
            given(postingService.createTransfer(any(), any(), any(), any(), any()))
                    .willThrow(new AccountNotFoundException(AccountId.of(TO)));

            MvcTestResult result = postTransfer(USER.toString(), readRequest("create-transfer.json"));

            assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
            assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
            assertThat(result).bodyJson().extractingPath("$.errorCode").isEqualTo("LEDGER-404-001");
        }
    }
}
