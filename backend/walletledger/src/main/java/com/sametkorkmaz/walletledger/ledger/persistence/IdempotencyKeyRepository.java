package com.sametkorkmaz.walletledger.ledger.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, IdempotencyKeyId> {
    @Modifying
    @Query(value = """
                INSERT INTO idempotency_keys (user_id, idempotency_key, request_hash)
                VALUES (:userId, :key, :hash)
                ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(UUID userId, UUID key, String hash);
}
