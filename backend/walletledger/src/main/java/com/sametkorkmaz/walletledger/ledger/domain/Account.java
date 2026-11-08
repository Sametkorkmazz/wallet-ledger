package com.sametkorkmaz.walletledger.ledger.domain;

import java.util.UUID;

public record Account(UUID id, UUID userId, Currency currency) {

}
