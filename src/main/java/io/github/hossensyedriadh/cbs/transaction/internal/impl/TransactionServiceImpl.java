package io.github.hossensyedriadh.cbs.transaction.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.LedgerService;
import io.github.hossensyedriadh.cbs.product.api.CurrencyService;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.transaction.api.TransactionService;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;
import io.github.hossensyedriadh.cbs.transaction.api.model.CreateDepositCommand;
import io.github.hossensyedriadh.cbs.transaction.api.model.CreateTransferCommand;
import io.github.hossensyedriadh.cbs.transaction.api.model.CreateWithdrawalCommand;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;
import io.github.hossensyedriadh.cbs.transaction.internal.mapper.TransactionMapper;
import io.github.hossensyedriadh.cbs.transaction.internal.repository.TransactionRepository;
import io.github.hossensyedriadh.cbs.transaction.internal.util.TransactionReferenceGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final CurrencyService currencyService;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository, TransactionMapper transactionMapper,
                                  AccountService accountService, LedgerService ledgerService,
                                  CurrencyService currencyService, HttpServletRequest httpServletRequest) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountService = accountService;
        this.ledgerService = ledgerService;
        this.currencyService = currencyService;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Optional<TransactionView> getTransaction(Long id) {
        return this.transactionRepository.findById(id).map(this.transactionMapper::toView);
    }

    @Override
    public Optional<TransactionView> getTransactionByReference(String reference) {
        return this.transactionRepository.findByTransactionReference(reference).map(this.transactionMapper::toView);
    }

    @Override
    @Transactional
    public TransactionView deposit(CreateDepositCommand command) {
        AccountView account = this.getActiveAccount(command.getAccountId());

        this.validateCurrency(command.getCurrencyId(), account.getCurrencyId());

        Transaction transaction = createTransaction(account.getId(), null, command.getCurrencyId(), TransactionType.DEPOSIT,
                command.getAmount(), command.getDescription());

        Transaction savedTransaction = this.transactionRepository.save(transaction);

        log.info("Deposit transaction saved: {}", savedTransaction.getId());

        Long customerLedgerAccountId = this.ledgerService.getLedgerAccountByAccountId(account.getId()).orElseThrow(() -> {
            log.warn("No ledger account mapped to account: {}", account.getId());
            return new ApiResourceException("No ledger account mapped to account", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        this.ledgerService.postDeposit(savedTransaction.getId(), customerLedgerAccountId, command.getAmount());

        this.completeTransaction(savedTransaction);

        return transactionMapper.toView(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionView withdraw(CreateWithdrawalCommand command) {
        AccountView account = this.getActiveAccount(command.getAccountId());

        this.validateCurrency(command.getCurrencyId(), account.getCurrencyId());

        Transaction transaction = this.createTransaction(account.getId(), null, command.getCurrencyId(),
                TransactionType.WITHDRAWAL, command.getAmount(), command.getDescription());

        Transaction savedTransaction = this.transactionRepository.save(transaction);

        log.info("Withdrawal transaction saved: {}", savedTransaction.getId());

        Long customerLedgerAccountId = this.ledgerService.getLedgerAccountByAccountId(account.getId()).orElseThrow(() -> {
            log.warn("No ledger account mapped to account: {}", account.getId());
            return new ApiResourceException("No ledger account mapped to account", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        this.ledgerService.postWithdrawal(savedTransaction.getId(), customerLedgerAccountId, command.getAmount());

        this.completeTransaction(savedTransaction);

        return transactionMapper.toView(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionView transfer(CreateTransferCommand command) {
        if (command.getAccountId().equals(command.getDestinationAccountId())) {
            log.warn("Source and destination accounts must be different");
            throw new ApiResourceException("Source and destination accounts must be different", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        AccountView sourceAccount = this.getActiveAccount(command.getAccountId());
        AccountView destinationAccount = this.getActiveAccount(command.getDestinationAccountId());

        this.validateCurrency(command.getCurrencyId(), sourceAccount.getCurrencyId());
        this.validateCurrency(command.getCurrencyId(), destinationAccount.getCurrencyId());

        Transaction transaction = this.createTransaction(sourceAccount.getId(), destinationAccount.getId(),
                command.getCurrencyId(), TransactionType.TRANSFER, command.getAmount(), command.getDescription());

        Transaction savedTransaction = this.transactionRepository.save(transaction);

        log.info("Transfer transaction saved: {}", savedTransaction.getId());

        Long sourceLedgerAccountId = this.ledgerService.getLedgerAccountByAccountId(sourceAccount.getId()).orElseThrow(() -> {
            log.warn("No ledger account mapped to source account: {}", sourceAccount.getId());
            return new ApiResourceException("No ledger account mapped to source account", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        Long destinationLedgerAccountId = this.ledgerService.getLedgerAccountByAccountId(destinationAccount.getId()).orElseThrow(() -> {
            log.warn("No ledger account mapped to destination account: {}", destinationAccount.getId());
            return new ApiResourceException("No ledger account mapped to destination account");
        });

        this.ledgerService.postTransfer(savedTransaction.getId(), sourceLedgerAccountId, destinationLedgerAccountId, command.getAmount());

        this.completeTransaction(savedTransaction);

        return this.transactionMapper.toView(savedTransaction);
    }

    private AccountView getActiveAccount(Long accountId) {
        AccountView account = this.accountService.getAccount(accountId).orElseThrow(() -> {
            log.warn("Account not found with ID: {}", accountId);
            return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account with ID: {} is not active", accountId);
            throw new ApiResourceException("Account not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        return account;
    }

    private void validateCurrency(Long transactionCurrencyId, Long accountCurrencyId) {
        if (!transactionCurrencyId.equals(accountCurrencyId)) {
            log.warn("Transaction currency {} does not match account currency {}", transactionCurrencyId, accountCurrencyId);
            throw new ApiResourceException("Transaction currency does not match account currency");
        }

        this.currencyService.getCurrency(transactionCurrencyId).filter(CurrencyView::isActive).orElseThrow(() -> {
            log.warn("Current currency {} is not active", transactionCurrencyId);
            return new ApiResourceException("Currency is inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    private Transaction createTransaction(Long accountId, Long destinationAccountId, Long currencyId,
                                          TransactionType transactionType, BigDecimal amount, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(TransactionReferenceGenerator.generate(transactionType));
        transaction.setAccountId(accountId);
        transaction.setDestinationAccountId(destinationAccountId);
        transaction.setCurrencyId(currencyId);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(description);

        return transaction;
    }

    private void completeTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(OffsetDateTime.now());

        this.transactionRepository.save(transaction);

        log.info("Transaction with ID: {} marked as completed", transaction.getId());
    }
}
