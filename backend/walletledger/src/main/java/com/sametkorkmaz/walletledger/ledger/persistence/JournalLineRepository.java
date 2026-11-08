package com.sametkorkmaz.walletledger.ledger.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

interface JournalLineRepository extends JpaRepository<JournalLineEntity, UUID> {
    List<JournalLineEntity> findByEntryId(UUID entryId);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM JournalLineEntity l WHERE l.accountId = :accountId")
    BigDecimal sumAmountByAccountId(@Param("accountId") UUID accountId);
}
