package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.AccountHoldService;
import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHold;
import io.github.hossensyedriadh.cbs.account.internal.mapper.AccountHoldMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHoldRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountHoldServiceImpl implements AccountHoldService {
    private final AccountHoldRepository accountHoldRepository;
    private final AccountService accountService;
    private final AccountBalanceService accountBalanceService;
    private final AccountHoldMapper accountHoldMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AccountHoldServiceImpl(AccountHoldRepository accountHoldRepository, AccountService accountService,
                                  AccountBalanceService accountBalanceService, AccountHoldMapper accountHoldMapper,
                                  HttpServletRequest httpServletRequest) {
        this.accountHoldRepository = accountHoldRepository;
        this.accountService = accountService;
        this.accountBalanceService = accountBalanceService;
        this.accountHoldMapper = accountHoldMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<AccountHoldView> getHolds(Long accountId, AccountHoldStatus status) {
        return this.accountHoldRepository.findByAccountIdAndStatus(accountId, status).stream()
                .map(this.accountHoldMapper::toView)
                .toList();
    }

    @Override
    public Optional<AccountHoldView> getHold(Long holdId) {
        return this.accountHoldRepository.findById(holdId).map(this.accountHoldMapper::toView);
    }

    @Transactional
    @Override
    public AccountHoldView createHold(CreateAccountHoldCommand command) {
        AccountView account = this.accountService.getAccountForUpdate(command.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", command.getAccountId());
                    return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account ID: {} is not active", command.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.accountBalanceService.getBalance(account.getId()).ifPresentOrElse(balance -> {
            if (command.getAmount().compareTo(balance.getAvailableBalance()) > 0) {
                log.warn("Insufficient funds to create account hold for account ID: {}", command.getAccountId());
                throw new ApiResourceException("Insufficient funds", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
        }, () -> {
            log.warn("Account ID: {} has no balance records", command.getAccountId());
            throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        AccountHold hold = this.accountHoldMapper.toEntity(command);
        AccountHold saved = this.accountHoldRepository.save(hold);

        log.info("Created hold of amount: {} for account: {}", command.getAmount(), saved);

        return this.accountHoldMapper.toView(saved);
    }

    @Transactional
    @Override
    public AccountHoldView releaseHold(Long holdId) {
        AccountHold hold = this.accountHoldRepository.findByIdForUpdate(holdId).orElseThrow(() -> {
            log.warn("Account hold not found with ID: {}", holdId);
            return new ApiResourceException("Account hold not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (hold.getStatus() != AccountHoldStatus.ACTIVE) {
            log.warn("Account hold ID: {} is not active", holdId);
            throw new ApiResourceException("Account hold is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        hold.setStatus(AccountHoldStatus.RELEASED);
        hold.setReleasedAt(OffsetDateTime.now());

        AccountHold saved = this.accountHoldRepository.save(hold);

        log.info("Released hold of amount: {} for account: {}, hold: {}", hold.getAmount(), saved.getAccountId(), saved.getId());

        return this.accountHoldMapper.toView(saved);
    }
}
