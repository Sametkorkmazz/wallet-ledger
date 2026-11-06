package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.Currency;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "USER_ID", nullable = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CURRENCY", nullable = false)
    private Currency currency;

    @Column(name = "CREATED_AT", insertable = false, updatable = false, nullable = false)
    private Instant createdAt;

    protected AccountEntity() {
    }

    public AccountEntity(UUID userId, Currency currency) {
        this.userId = userId;
        this.currency = currency;
        this.createdAt = Instant.now();
    }
}
