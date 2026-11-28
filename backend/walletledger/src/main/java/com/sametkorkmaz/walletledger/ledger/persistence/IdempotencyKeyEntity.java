package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Getter
public class IdempotencyKeyEntity {
    @EmbeddedId
    private IdempotencyKeyId id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 64, nullable = false)
    private String requestHash;

    @Column(nullable = true)
    private UUID entryId;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(nullable = true)
    private Integer responseStatus;

    @Column(nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private String responseBody;

    @Generated(event = EventType.INSERT)
    @Column(nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    public void complete(EntryId entryId, int status, String body) {
        Objects.requireNonNull(entryId, "entryId must not be null");
        Objects.requireNonNull(body, "body must not be null");
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("Invalid http status");
        }
        this.entryId = entryId.value();
        this.responseStatus = status;
        this.responseBody = body;
    }
}

@Embeddable
record IdempotencyKeyId(@Column(name = "idempotency_key")
                        UUID idempotencyKey,
                        @Column(name = "user_id")
                        UUID userId) {
}
