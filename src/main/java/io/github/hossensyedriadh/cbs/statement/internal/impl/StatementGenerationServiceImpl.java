package io.github.hossensyedriadh.cbs.statement.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementStatus;
import io.github.hossensyedriadh.cbs.statement.api.enumerator.StatementType;
import io.github.hossensyedriadh.cbs.statement.internal.domain.Statement;
import io.github.hossensyedriadh.cbs.statement.internal.domain.StatementEntry;
import io.github.hossensyedriadh.cbs.statement.internal.repository.StatementEntryRepository;
import io.github.hossensyedriadh.cbs.statement.internal.repository.StatementRepository;
import io.github.hossensyedriadh.cbs.statement.internal.service.StatementBalanceService;
import io.github.hossensyedriadh.cbs.statement.internal.service.StatementGenerationService;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Log4j2
@Service
public class StatementGenerationServiceImpl implements StatementGenerationService {
    private final StatementRepository statementRepository;
    private final StatementEntryRepository statementEntryRepository;
    private final StatementBalanceService statementBalanceService;
    private final TransactionService transactionService;
    private final AccountService accountService;

    @Value("${cbs.timezone}")
    private ZoneId cbsTimezone;

    @Autowired
    public StatementGenerationServiceImpl(StatementRepository statementRepository, StatementEntryRepository statementEntryRepository,
                                          StatementBalanceService statementBalanceService, TransactionService transactionService,
                                          AccountService accountService) {
        this.statementRepository = statementRepository;
        this.statementEntryRepository = statementEntryRepository;
        this.statementBalanceService = statementBalanceService;
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @Transactional
    @Override
    public Statement generate(Long accountId, LocalDate start, LocalDate end, StatementType type) {
        this.validatePeriod(start, end);

        AccountView account = this.accountService.getAccount(accountId).orElseThrow(() -> {
            log.warn("Account with id {} not found", accountId);
            return new ApiResourceException("Account not found", HttpStatus.NOT_FOUND);
        });

        BigDecimal openingBalance = this.statementBalanceService.getOpeningBalance(accountId, start);

        OffsetDateTime fromDateTime = start.atStartOfDay(this.cbsTimezone).toOffsetDateTime();
        OffsetDateTime toDateTime = end.plusDays(1).atStartOfDay(this.cbsTimezone).toOffsetDateTime();

        List<TransactionView> transactions = this.transactionService.getTransactions(accountId, fromDateTime, toDateTime);

        BigDecimal runningBalance = openingBalance;
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        Statement statement = new Statement();
        statement.setAccountId(accountId);
        statement.setCurrencyId(account.getCurrencyId());
        statement.setStatementType(type);
        statement.setStatementPeriodStart(start);
        statement.setStatementPeriodEnd(end);
        statement.setOpeningBalance(openingBalance);
        statement.setTotalDebits(totalDebits);
        statement.setTotalCredits(totalCredits);
        statement.setStatus(StatementStatus.GENERATED);
        statement.setGeneratedAt(OffsetDateTime.now(this.cbsTimezone));

        Statement saved = this.statementRepository.save(statement);

        List<StatementEntry> entries = statement.getStatementEntries();

        for (TransactionView trx : transactions) {
            StatementEntryAmounts amounts = this.classifyTransaction(accountId, trx);

            if (amounts.debitAmount().signum() > 0) {
                runningBalance = runningBalance.subtract(amounts.debitAmount());
                totalDebits = totalDebits.add(amounts.debitAmount());
            }

            if (amounts.creditAmount().signum() > 0) {
                runningBalance = runningBalance.add(amounts.creditAmount());
                totalCredits = totalCredits.add(amounts.creditAmount());
            }

            StatementEntry entry = new StatementEntry();
            entry.setStatement(saved);
            entry.setTransactionId(trx.getId());
            entry.setTransactionReference(trx.getTransactionReference());
            entry.setTransactionDate(trx.getCreatedAt());
            entry.setValueDate(trx.getCreatedAt().toLocalDate());
            entry.setDescription(trx.getDescription());
            entry.setDebitAmount(amounts.debitAmount());
            entry.setCreditAmount(amounts.creditAmount());
            entry.setRunningBalance(runningBalance);

            entries.add(entry);
        }

        this.statementEntryRepository.saveAll(entries);

        saved.setClosingBalance(runningBalance);
        saved.setTotalDebits(totalDebits);
        saved.setTotalCredits(totalCredits);

        Statement finalized = this.statementRepository.save(saved);

        log.info("Statement [{}] generation for account: {} finished .", type, accountId);

        return finalized;
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Invalid statement period");
        }
    }

    private StatementEntryAmounts classifyTransaction(Long accountId, TransactionView transaction) {
        if (transaction.getTransactionType() == TransactionType.REVERSAL) {
            return this.classifyReversal(accountId, transaction);
        }

        if (accountId.equals(transaction.getDestinationAccountId())) {
            return StatementEntryAmounts.credit(transaction.getAmount());
        }

        if (!accountId.equals(transaction.getAccountId())) {
            log.error("Transaction {} does not belong account: {}", transaction.getId(), transaction.getAccountId());
            throw new IllegalStateException("Transaction " + transaction.getId() + " does not belong to account " + accountId);
        }

        return switch (transaction.getTransactionType()) {
            case WITHDRAWAL, TRANSFER, FEE -> StatementEntryAmounts.debit(transaction.getAmount());
            case DEPOSIT, INTEREST -> StatementEntryAmounts.credit(transaction.getAmount());
            default -> {
                log.error("Unsupported transaction type: {}", transaction.getTransactionType());
                throw new IllegalStateException("Unsupported transaction type for statement: " + transaction.getTransactionType());
            }
        };
    }

    private StatementEntryAmounts classifyReversal(Long accountId, TransactionView reversal) {
        Long originalTransactionId = reversal.getReversalOfTransactionId();

        if (originalTransactionId == null) {
            log.error("Reversal transaction {} has no original transaction", reversal.getId());
            throw new IllegalStateException("Reversal transaction " + reversal.getId() + " has no original transaction");
        }

        TransactionView original = this.transactionService.getTransaction(originalTransactionId).orElseThrow(() -> {
            log.error("Original transaction {} not found for reversal {}", originalTransactionId, reversal.getId());
            return new IllegalStateException("Original transaction " + originalTransactionId + " not found for reversal" + reversal.getId());
        });

        StatementEntryAmounts originalAmounts = this.classifyTransaction(accountId, original);

        if (originalAmounts.debitAmount().signum() > 0) {
            return StatementEntryAmounts.credit(originalAmounts.debitAmount());
        }

        if (originalAmounts.creditAmount().signum() > 0) {
            return StatementEntryAmounts.debit(originalAmounts.creditAmount());
        }

        log.error("Unable to determine statement direction for reversal: {}", reversal.getId());
        throw new IllegalStateException("Unable to determine statement direction for reversal " + reversal.getId());
    }

    private record StatementEntryAmounts(BigDecimal debitAmount, BigDecimal creditAmount) {
        static StatementEntryAmounts debit(BigDecimal amount) {
            return new StatementEntryAmounts(amount, BigDecimal.ZERO);
        }

        static StatementEntryAmounts credit(BigDecimal amount) {
            return new StatementEntryAmounts(BigDecimal.ZERO, amount);
        }
    }
}
