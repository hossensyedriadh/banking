package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.command.ReleaseAccountHoldCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.event.AccountHoldCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.AccountHoldReleasedEvent;
import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;
import io.github.hossensyedriadh.cbs.account.api.model.AccountHoldView;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountHoldService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHold;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHoldStatusHistory;
import io.github.hossensyedriadh.cbs.account.internal.mapper.AccountHoldMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHoldRepository;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHoldStatusHistoryRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountHoldServiceImpl implements AccountHoldService {
    private final AccountHoldRepository accountHoldRepository;
    private final AccountHoldStatusHistoryRepository accountHoldStatusHistoryRepository;
    private final AccountService accountService;
    private final AccountBalanceService accountBalanceService;
    private final AccountHoldMapper accountHoldMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HttpServletRequest httpServletRequest;

    @Value("${cbs.timezone}")
    private ZoneId cbsTimezone;

    @Autowired
    public AccountHoldServiceImpl(AccountHoldRepository accountHoldRepository, AccountHoldStatusHistoryRepository accountHoldStatusHistoryRepository,
                                  AccountService accountService, AccountBalanceService accountBalanceService, AccountHoldMapper accountHoldMapper,
                                  ApplicationEventPublisher applicationEventPublisher, HttpServletRequest httpServletRequest) {
        this.accountHoldRepository = accountHoldRepository;
        this.accountHoldStatusHistoryRepository = accountHoldStatusHistoryRepository;
        this.accountService = accountService;
        this.accountBalanceService = accountBalanceService;
        this.accountHoldMapper = accountHoldMapper;
        this.applicationEventPublisher = applicationEventPublisher;
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
    public AccountHoldView createHold(@Valid CreateAccountHoldCommand command) {
        AccountView account = this.accountService.getAccount(command.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", command.getAccountId());
                    return new ApiResourceException("Account not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
                });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account ID: {} is not active", command.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountBalanceView balance = this.accountBalanceService.getBalance(account.getId());

        if (command.getAmount().compareTo(balance.getAvailableBalance()) > 0) {
            log.warn("Insufficient funds to create account hold for account ID: {}", command.getAccountId());
            throw new ApiResourceException("Insufficient funds", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountHold hold = this.accountHoldMapper.toEntity(command);
        AccountHold saved = this.accountHoldRepository.save(hold);

        this.addStatusHistory(saved, AccountHoldStatus.ACTIVE, command.getReason(), saved.getCreatedAt());

        this.applicationEventPublisher.publishEvent(new AccountHoldCreatedEvent(saved.getId(), command.getReason()));

        log.info("Created hold of amount: {} for account: {}", command.getAmount(), saved);

        return this.accountHoldMapper.toView(saved);
    }

    @Transactional
    @Override
    public AccountHoldView releaseHold(ReleaseAccountHoldCommand command) {
        AccountHold hold = this.accountHoldRepository.findByIdForUpdate(command.getHoldId()).orElseThrow(() -> {
            log.warn("Account hold not found with ID: {}", command.getHoldId());
            return new ApiResourceException("Account hold not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });

        AccountHoldStatusHistory history = this.accountHoldStatusHistoryRepository.findFirstByAccountHold_IdOrderByCreatedAtDescIdDesc(hold.getId())
                .orElseThrow(() -> {
                    log.warn("Account hold status history not found for hold ID: {}", command.getHoldId());
                    return new ApiResourceException("No history found for Account Hold", HttpStatus.NOT_FOUND, this.httpServletRequest);
                });

        if (history.getStatus() != AccountHoldStatus.ACTIVE) {
            log.warn("Account hold ID: {} is not active", command.getHoldId());
            throw new ApiResourceException("Account hold is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.addStatusHistory(hold, AccountHoldStatus.RELEASED, command.getReason(), OffsetDateTime.now(ZoneOffset.UTC));

        AccountHold saved = this.accountHoldRepository.save(hold);

        this.applicationEventPublisher.publishEvent(new AccountHoldReleasedEvent(saved.getId(), command.getReason()));

        log.info("Released hold of amount: {} for account: {}, hold: {}", hold.getAmount(), saved.getAccountId(), saved.getId());

        return this.accountHoldMapper.toView(saved);
    }

    @Transactional
    @Override
    public void expireHolds(LocalDate businessDate) {
        OffsetDateTime expirationBoundary = businessDate.plusDays(1).atStartOfDay(this.cbsTimezone).toOffsetDateTime();

        List<AccountHold> accountHolds = this.accountHoldRepository.findAllByStatusAndExpiresAtBefore(AccountHoldStatus.ACTIVE, expirationBoundary);

        List<AccountHoldStatusHistory> expired = accountHolds.stream().map(hold -> this.buildStatusHistory(hold, AccountHoldStatus.EXPIRED,
                "Auto expire", OffsetDateTime.now(this.cbsTimezone))).toList();

        this.accountHoldStatusHistoryRepository.saveAll(expired);

        log.info("Expired {} account balance holds expiring before: {}", accountHolds.size(), expirationBoundary.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private void addStatusHistory(AccountHold hold, AccountHoldStatus status, String reason, OffsetDateTime createdAt) {
        AccountHoldStatusHistory history = new AccountHoldStatusHistory();
        history.setStatus(status);
        history.setReason(reason);
        history.setAccountHold(hold);
        history.setCreatedAt(createdAt);

        this.accountHoldStatusHistoryRepository.save(history);
    }

    private AccountHoldStatusHistory buildStatusHistory(AccountHold hold, AccountHoldStatus status, String reason, OffsetDateTime createdAt) {
        AccountHoldStatusHistory history = new AccountHoldStatusHistory();
        history.setStatus(status);
        history.setReason(reason);
        history.setAccountHold(hold);
        history.setCreatedAt(createdAt);

        return history;
    }
}
