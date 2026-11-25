package com.sametkorkmaz.walletledger.ledger.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTransferRequest(
        @NotNull(message = "fromAccountId must not be null") UUID fromAccountId,

        @NotNull(message = "toAccountId must not be null") UUID toAccountId,

        @NotBlank(message = "description must not be empty")
        @Size(max = 255, message = "description must be at max 255 characters")
        String description,

        @NotBlank(message = "amount must not be blank")
        String amount
) {
}
