package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.service.AccountMappingService;
import io.github.hossensyedriadh.cbs.account.internal.domain.Account;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountMappingServiceImpl implements AccountMappingService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountMappingServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Long getAccountProductId(Long accountId) {
        Optional<Account> account = this.accountRepository.findById(accountId);

        if (account.isPresent()) {
            return account.get().getProductId();
        }

        log.warn("Account not found for customer with Account ID: {}", accountId);
        throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST);
    }
}
