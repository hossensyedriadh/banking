package io.github.hossensyedriadh.cbs.statement.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.statement.api.command.GenerateMonthlyStatementCommand;
import io.github.hossensyedriadh.cbs.statement.api.command.GenerateOnDemandStatementCommand;
import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementType;
import io.github.hossensyedriadh.cbs.statement.api.event.MonthlyStatementGeneratedEvent;
import io.github.hossensyedriadh.cbs.statement.api.event.OnDemandStatementGeneratedEvent;
import io.github.hossensyedriadh.cbs.statement.api.model.StatementView;
import io.github.hossensyedriadh.cbs.statement.api.service.AccountStatementService;
import io.github.hossensyedriadh.cbs.statement.internal.domain.Statement;
import io.github.hossensyedriadh.cbs.statement.internal.mapper.StatementMapper;
import io.github.hossensyedriadh.cbs.statement.internal.repository.StatementRepository;
import io.github.hossensyedriadh.cbs.statement.internal.service.StatementGenerationService;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Log4j2
@Service
public class AccountStatementServiceImpl implements AccountStatementService {
    private final StatementRepository statementRepository;
    private final StatementGenerationService statementGenerationService;
    private final AccountService accountService;
    private final StatementMapper statementMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AccountStatementServiceImpl(StatementRepository statementRepository, StatementGenerationService statementGenerationService,
                                       AccountService accountService, StatementMapper statementMapper,
                                       ApplicationEventPublisher applicationEventPublisher) {
        this.statementRepository = statementRepository;
        this.statementGenerationService = statementGenerationService;
        this.accountService = accountService;
        this.statementMapper = statementMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StatementView> getStatements(Long accountId, Pageable pageable) {
        this.validateAccount(accountId);

        return this.statementRepository.findByAccountId(accountId, pageable).map(this.statementMapper::toView);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<StatementView> getStatement(Long accountId, Long statementId) {
        return this.statementRepository.findByIdAndAccountId(statementId, accountId).map(this.statementMapper::toView);
    }

    @Transactional
    @Override
    public StatementView generateMonthlyStatement(@Valid GenerateMonthlyStatementCommand command) {
        this.validateAccount(command.getAccountId());

        LocalDate periodStart = command.getPeriod().atDay(1);
        LocalDate periodEnd = command.getPeriod().atEndOfMonth();

        Optional<Statement> existingStatement = this.statementRepository.findByAccountIdAndStatementPeriodStartAndStatementPeriodEndAndStatementType(
                command.getAccountId(), periodStart, periodEnd, StatementType.MONTHLY);

        if (existingStatement.isPresent()) {
            log.info("Monthly statement already exists for account: {}, period: {}", command.getAccountId(), command.getPeriod());

            return this.statementMapper.toView(existingStatement.get());
        }

        Statement statement = this.statementGenerationService.generate(command.getAccountId(), periodStart, periodEnd, StatementType.MONTHLY);

        this.applicationEventPublisher.publishEvent(new MonthlyStatementGeneratedEvent(statement.getId(), command.getPeriod()));

        log.info("Monthly statement generated for account: {}, period: {}", command.getAccountId(), command.getPeriod());

        return this.statementMapper.toView(statement);
    }

    @Transactional
    @Override
    public StatementView generateOnDemandStatement(@Valid GenerateOnDemandStatementCommand command) {
        this.validateAccount(command.getAccountId());
        this.validatePeriod(command.getFromDate(), command.getToDate());

        Statement statement = this.statementGenerationService.generate(command.getAccountId(), command.getFromDate(),
                command.getToDate(), StatementType.ON_DEMAND);

        this.applicationEventPublisher.publishEvent(new OnDemandStatementGeneratedEvent(statement.getId(),
                command.getFromDate(), command.getToDate()));

        log.info("On-demand statement generated for account: {}, from: {}, to: {}", command.getAccountId(),
                command.getFromDate(), command.getToDate());

        return this.statementMapper.toView(statement);
    }

    private void validateAccount(Long accountId) {
        this.accountService.getAccount(accountId).orElseThrow(() -> {
            log.warn("Account with id {} not found", accountId);
            return new ApiResourceException("Account not found", HttpStatus.NOT_FOUND);
        });
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Statement period cannot be null");
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Statement start date cannot be after end date");
        }
    }
}
