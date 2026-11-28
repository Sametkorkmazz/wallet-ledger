package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.EntryId;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Stores idempotency keys and their responses (ADR 2).
 * <p>
 * Must be called inside a service transaction (05-transactions rule 2): this class opens none, so a call
 * outside one commits each statement on its own.
 */
@Component
@RequiredArgsConstructor
public class IdempotencyStore {
    private static final int SUCCESSFUL_INSERT = 1;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public boolean tryInsert(UserId userId, UUID key, String hash) {
        int result = idempotencyKeyRepository.insertIfAbsent(userId.value(), key, hash);
        return result == SUCCESSFUL_INSERT;
    }

    public Optional<StoredKey> find(UserId userId, UUID key) {
        return idempotencyKeyRepository.findById(new IdempotencyKeyId(key, userId.value()))
                .map(e -> new StoredKey(e.getRequestHash(), EntryId.of(e.getEntryId()), e.getResponseStatus(), e.getResponseBody()));
    }

    public void complete(UserId userId, UUID key, EntryId entryId, int status, String body) {
        IdempotencyKeyEntity idempotencyKey = idempotencyKeyRepository.findById(new IdempotencyKeyId(key, userId.value()))
                .orElseThrow(() -> new IllegalStateException("Idempotency key not found"));
        idempotencyKey.complete(entryId, status, body);
        idempotencyKeyRepository.saveAndFlush(idempotencyKey);
    }
}
