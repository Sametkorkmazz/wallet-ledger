package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountStore {
    private final AccountRepository accountRepository;

    /**
     * Intended for a handful of ids; chunk callers if you need thousands
     */
    public Map<UUID, Account> findByIds(Collection<UUID> ids) {
        return accountRepository.findAllById(ids).stream().collect(Collectors.toMap(AccountEntity::getId, this::toAccount));
    }

    private Account toAccount(AccountEntity accountEntity) {
        return new Account(accountEntity.getId(), accountEntity.getUserId(), accountEntity.getCurrency());
    }
}
