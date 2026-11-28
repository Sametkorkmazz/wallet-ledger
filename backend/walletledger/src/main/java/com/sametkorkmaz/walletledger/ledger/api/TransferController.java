package com.sametkorkmaz.walletledger.ledger.api;

import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import com.sametkorkmaz.walletledger.ledger.service.PostingService;
import com.sametkorkmaz.walletledger.ledger.service.TransferResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {
    private static final String REPLAYED_HEADER = "Idempotent-Replayed";

    private final PostingService postingService;

    @PostMapping
    public ResponseEntity<CreateTransferResponse> create(@RequestHeader(name = "Idempotency-Key") UUID idempotencyKey,
                                                         @RequestHeader(name = "X-User-Id") UUID userId,// temporary: replaced by the JWT principal (Dec 7)
                                                         @Valid @RequestBody CreateTransferRequest request) {
        TransferResult result = postingService.createTransfer(UserId.of(userId), idempotencyKey, AccountId.of(request.fromAccountId()), AccountId.of(request.toAccountId()),
                request.amount(), request.description());

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .build(result.entryId().value());
        CreateTransferResponse body = new CreateTransferResponse(result.entryId().value());
        ResponseEntity.BodyBuilder response = ResponseEntity.created(uri);
        if (result.replayed()) {
            response.header(REPLAYED_HEADER, "true");
        }
        return response.body(body);
    }
}
