package com.sametkorkmaz.walletledger.ledger.persistence;

import com.sametkorkmaz.walletledger.ledger.domain.Account;
import com.sametkorkmaz.walletledger.ledger.domain.AccountId;
import com.sametkorkmaz.walletledger.ledger.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountStore {
    private final AccountRepository accountRepository;

    /**
     * Intended for a handful of ids; chunk callers if you need thousands
     */
    public Map<AccountId, Account> findByIds(Collection<AccountId> ids) {
        return accountRepository.findAllById(ids.stream().map(AccountId::value).toList())
                .stream().collect(Collectors.toMap(account -> AccountId.of(account.getId()),
                        this::toAccount));
    }

    private Account toAccount(AccountEntity accountEntity) {
        return new Account(AccountId.of(accountEntity.getId()), accountEntity.getUserId() == null ? null : new UserId(accountEntity.getUserId()),
                accountEntity.getCurrency());
    }
}
