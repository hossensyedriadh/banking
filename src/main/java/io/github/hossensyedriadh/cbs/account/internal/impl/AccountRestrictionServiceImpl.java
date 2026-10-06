package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.command.ReleaseAccountRestrictionCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionType;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.event.AccountRestrictionCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.model.AccountRestrictionView;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountRestrictionService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountRestriction;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountRestrictionStatusHistory;
import io.github.hossensyedriadh.cbs.account.internal.mapper.AccountRestrictionMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRestrictionRepository;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRestrictionStatusHistoryRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountRestrictionServiceImpl implements AccountRestrictionService {
    private final AccountRestrictionRepository accountRestrictionRepository;
    private final AccountRestrictionStatusHistoryRepository accountRestrictionStatusHistoryRepository;
    private final AccountService accountService;
    private final AccountRestrictionMapper accountRestrictionMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AccountRestrictionServiceImpl(AccountRestrictionRepository accountRestrictionRepository, AccountRestrictionStatusHistoryRepository accountRestrictionStatusHistoryRepository,
                                         AccountService accountService, AccountRestrictionMapper accountRestrictionMapper, ApplicationEventPublisher applicationEventPublisher,
                                         HttpServletRequest httpServletRequest) {
        this.accountRestrictionRepository = accountRestrictionRepository;
        this.accountRestrictionStatusHistoryRepository = accountRestrictionStatusHistoryRepository;
        this.accountService = accountService;
        this.accountRestrictionMapper = accountRestrictionMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<AccountRestrictionView> getRestrictions(Long accountId, AccountRestrictionStatus status) {
        return this.accountRestrictionRepository.findByAccountIdAndStatus(accountId, status).stream()
                .map(this.accountRestrictionMapper::toView).toList();
    }

    @Override
    public Optional<AccountRestrictionView> getRestriction(Long restrictionId) {
        return this.accountRestrictionRepository.findById(restrictionId).map(this.accountRestrictionMapper::toView);
    }

    @Transactional
    @Override
    public AccountRestrictionView createRestriction(@Valid CreateAccountRestrictionCommand command) {
        AccountView account = this.accountService.getAccountForUpdate(command.getAccountId())
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", command.getAccountId());
                    return new ApiResourceException("Account not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
                });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account ID: {} is not active", command.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        boolean alreadyActive = this.accountRestrictionRepository.existsByAccountIdAndRestrictionTypeAndStatus(account.getId(),
                command.getRestrictionType(), AccountRestrictionStatus.ACTIVE);

        if (alreadyActive) {
            log.warn("Restriction on {} for Account ID: {} is already active", command.getAccountId().toString(), command.getAccountId());
            throw new ApiResourceException("Restriction is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountRestriction restriction = this.accountRestrictionMapper.toEntity(command);
        AccountRestriction saved = this.accountRestrictionRepository.save(restriction);

        this.addStatusHistory(saved, AccountRestrictionStatus.ACTIVE, command.getReason(), saved.getCreatedAt());

        this.applicationEventPublisher.publishEvent(new AccountRestrictionCreatedEvent(saved.getId(), command.getReason()));

        log.info("Created restriction of type: {} for Account ID: {}", restriction.getRestrictionType(), account.getId());

        return this.accountRestrictionMapper.toView(saved);
    }

    @Transactional
    @Override
    public AccountRestrictionView releaseRestriction(ReleaseAccountRestrictionCommand command) {
        AccountRestriction restriction = this.accountRestrictionRepository.findByIdForUpdate(command.getRestrictionId())
                .orElseThrow(() -> {
                    log.warn("Account restriction not found with ID: {}", command.getRestrictionId());
                    return new ApiResourceException("Account restriction not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
                });

        AccountRestrictionStatusHistory history = this.accountRestrictionStatusHistoryRepository.findFirstByAccountRestriction_IdOrderByCreatedAtDescIdDesc(restriction.getId())
                .orElseThrow(() -> {
                    log.warn("Account restriction status history not found for restriction ID: {}", restriction.getId());
                    return new ApiResourceException("No history found for Account restriction", HttpStatus.NOT_FOUND, this.httpServletRequest);
                });

        if (history.getStatus() != AccountRestrictionStatus.ACTIVE) {
            log.warn("Account restriction: {} is not active", restriction.getAccountId());
            throw new ApiResourceException("Restriction is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        this.addStatusHistory(restriction, AccountRestrictionStatus.RELEASED, command.getReason(), OffsetDateTime.now(ZoneOffset.UTC));

        AccountRestriction saved = this.accountRestrictionRepository.save(restriction);

        this.applicationEventPublisher.publishEvent(new AccountRestrictionCreatedEvent(saved.getId(), command.getReason()));

        log.info("Released restriction of type: {} for Account ID: {}", restriction.getRestrictionType(), restriction.getAccountId());

        return this.accountRestrictionMapper.toView(saved);
    }

    @Override
    public void validateDebitAllowed(Long accountId) {
        Set<AccountRestrictionType> restrictions = new HashSet<>(this.accountRestrictionRepository.findActiveRestrictionTypes(accountId));

        if (restrictions.contains(AccountRestrictionType.DEBIT_BLOCK) || restrictions.contains(AccountRestrictionType.FULL_FREEZE)) {
            log.warn("Account ID: {} is debit restricted", accountId);
            throw new ApiResourceException("Debit transactions are not allowed for this account", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void validateCreditAllowed(Long accountId) {
        Set<AccountRestrictionType> restrictions = new HashSet<>(this.accountRestrictionRepository.findActiveRestrictionTypes(accountId));

        if (restrictions.contains(AccountRestrictionType.CREDIT_BLOCK) || restrictions.contains(AccountRestrictionType.FULL_FREEZE)) {
            log.warn("Account ID: {} is credit restricted", accountId);
            throw new ApiResourceException("Credit transactions are not allowed for this account", HttpStatus.BAD_REQUEST);
        }
    }

    private void addStatusHistory(AccountRestriction restriction, AccountRestrictionStatus status, String reason, OffsetDateTime createdAt) {
        AccountRestrictionStatusHistory history = new AccountRestrictionStatusHistory();
        history.setStatus(status);
        history.setReason(reason);
        history.setAccountRestriction(restriction);
        history.setCreatedAt(createdAt);

        this.accountRestrictionStatusHistoryRepository.save(history);
    }
}
