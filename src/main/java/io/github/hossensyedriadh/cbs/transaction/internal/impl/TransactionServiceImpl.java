package io.github.hossensyedriadh.cbs.transaction.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.LedgerService;
import io.github.hossensyedriadh.cbs.product.api.CurrencyService;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.transaction.api.TransactionService;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionDirection;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.cbs.transaction.api.enumerator.TransactionType;
import io.github.hossensyedriadh.cbs.transaction.api.model.*;
import io.github.hossensyedriadh.cbs.transaction.internal.domain.Transaction;
import io.github.hossensyedriadh.cbs.transaction.internal.mapper.TransactionMapper;
import io.github.hossensyedriadh.cbs.transaction.internal.repository.TransactionRepository;
import io.github.hossensyedriadh.cbs.transaction.internal.util.TransactionReferenceGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<AccountTransactionView> getAccountTransactions(Long accountId, Pageable pageable) {
        return this.transactionRepository.findAccountTransactions(accountId, pageable)
                .map(transaction -> this.toAccountTransactionView(transaction, accountId));
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
        Optional<TransactionView> existingTransaction = this.getCompletedOrPendingTransaction(command.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            log.info("Duplicate deposit request detected for idempotency key: {}", command.getIdempotencyKey());

            return existingTransaction.get();
        }

        AccountView account = this.getActiveAccount(command.getAccountId());

        this.validateCurrency(command.getCurrencyId(), account.getCurrencyId());

        Transaction transaction = createTransaction(account.getId(), null, command.getCurrencyId(), TransactionType.DEPOSIT,
                command.getAmount(), command.getDescription(), command.getIdempotencyKey());

        Transaction savedTransaction = this.transactionRepository.save(transaction);

        log.info("Deposit transaction saved: {}", savedTransaction.getId());

        Long customerLedgerAccountId = this.getCustomerLedgerAccountId(account.getId());

        this.ledgerService.postDeposit(savedTransaction.getId(), customerLedgerAccountId, command.getAmount());

        this.completeTransaction(savedTransaction);

        return transactionMapper.toView(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionView withdraw(CreateWithdrawalCommand command) {
        Optional<TransactionView> existingTransaction = this.getCompletedOrPendingTransaction(command.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            log.info("Duplicate withdrawal request detected for idempotency key: {}", command.getIdempotencyKey());

            return existingTransaction.get();
        }

        AccountView account = this.getActiveAccount(command.getAccountId());

        this.validateCurrency(command.getCurrencyId(), account.getCurrencyId());

        Transaction transaction = this.createTransaction(account.getId(), null, command.getCurrencyId(),
                TransactionType.WITHDRAWAL, command.getAmount(), command.getDescription(), command.getIdempotencyKey());

        Transaction savedTransaction = this.transactionRepository.save(transaction);

        log.info("Withdrawal transaction saved: {}", savedTransaction.getId());

        Long customerLedgerAccountId = this.getCustomerLedgerAccountId(account.getId());

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

        Optional<TransactionView> existingTransaction = this.getCompletedOrPendingTransaction(command.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            log.info("Duplicate transfer request detected for idempotency key: {}", command.getIdempotencyKey());

            return existingTransaction.get();
        }

        AccountView sourceAccount = this.getActiveAccount(command.getAccountId());
        AccountView destinationAccount = this.getActiveAccount(command.getDestinationAccountId());

        this.validateCurrency(command.getCurrencyId(), sourceAccount.getCurrencyId());
        this.validateCurrency(command.getCurrencyId(), destinationAccount.getCurrencyId());

        Transaction transaction = this.createTransaction(sourceAccount.getId(), destinationAccount.getId(),
                command.getCurrencyId(), TransactionType.TRANSFER, command.getAmount(), command.getDescription(),
                command.getIdempotencyKey());

        Transaction savedTransaction = this.transactionRepository.save(transaction);

        log.info("Transfer transaction saved: {}", savedTransaction.getId());

        Long sourceLedgerAccountId = this.getCustomerLedgerAccountId(sourceAccount.getId());

        Long destinationLedgerAccountId = this.getCustomerLedgerAccountId(destinationAccount.getId());

        this.ledgerService.postCustomerTransfer(savedTransaction.getId(), sourceLedgerAccountId, destinationLedgerAccountId, command.getAmount());

        this.completeTransaction(savedTransaction);

        return this.transactionMapper.toView(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionView reverse(ReverseTransactionCommand command) {
        Optional<TransactionView> existingTransaction = this.getCompletedOrPendingTransaction(command.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            log.info("Duplicate reversal request detected for idempotency key: {}", command.getIdempotencyKey());

            return existingTransaction.get();
        }

        Transaction originalTransaction = this.transactionRepository.findById(command.getTransactionId())
                .orElseThrow(() -> {
                    log.warn("Transaction not found with ID {}", command.getTransactionId());
                    return new ApiResourceException("Transaction not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (originalTransaction.getStatus() != TransactionStatus.COMPLETED) {
            log.warn("Transaction {} can not be reversed because it's status is {}", originalTransaction.getId(), originalTransaction.getStatus());
            throw new ApiResourceException("Only completed transactions can be reversed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (originalTransaction.getReversalOfTransactionId() != null) {
            log.warn("Transaction {} is already a reversal", originalTransaction.getId());
            throw new ApiResourceException("A reversal can not be reversed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        boolean alreadyReversed = this.transactionRepository.existsByReversalOfTransactionId(originalTransaction.getId());

        if (alreadyReversed) {
            log.warn("Transaction {} has already been reversed", originalTransaction.getId());
            throw new ApiResourceException("Transaction has already been reversed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        Transaction reversal = this.createTransaction(originalTransaction.getAccountId(), originalTransaction.getDestinationAccountId(),
                originalTransaction.getCurrencyId(), originalTransaction.getTransactionType(), originalTransaction.getAmount(),
                command.getDescription(), command.getIdempotencyKey());
        reversal.setReversalOfTransactionId(originalTransaction.getId());

        Transaction savedReversal = this.transactionRepository.save(reversal);

        log.info("Reversal transaction created: id={}, originalTransactionId={}", savedReversal.getId(), originalTransaction.getId());

        this.postReversal(originalTransaction, savedReversal);

        this.completeTransaction(savedReversal);

        return this.transactionMapper.toView(savedReversal);
    }

    private void postReversal(Transaction originalTransaction, Transaction reversalTransaction) {
        switch (originalTransaction.getTransactionType()) {
            case DEPOSIT -> this.reverseDeposit(originalTransaction, reversalTransaction);
            case WITHDRAWAL -> this.reverseWithdrawal(originalTransaction, reversalTransaction);
            case TRANSFER -> this.reverseTransfer(originalTransaction, reversalTransaction);
            default -> {
                log.warn("Unsupported transaction type for reversal: {}", originalTransaction.getTransactionType());
                throw new ApiResourceException("Unsupported transaction type for reversal", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
        }
    }

    private void reverseDeposit(Transaction originalTransaction, Transaction reversalTransaction) {
        Long customerLedgerAccountId = this.getCustomerLedgerAccountId(originalTransaction.getAccountId());

        this.ledgerService.postWithdrawal(reversalTransaction.getId(), customerLedgerAccountId, originalTransaction.getAmount());
    }

    private void reverseWithdrawal(Transaction originalTransaction, Transaction reversalTransaction) {
        Long customerLedgerAccountId = this.getCustomerLedgerAccountId(originalTransaction.getAccountId());

        this.ledgerService.postDeposit(reversalTransaction.getId(), customerLedgerAccountId, originalTransaction.getAmount());
    }

    private void reverseTransfer(Transaction originalTransaction, Transaction reversalTransaction) {
        Long sourceLedgerAccountId = this.getCustomerLedgerAccountId(originalTransaction.getAccountId());
        Long destinationLedgerAccountId = this.getCustomerLedgerAccountId(originalTransaction.getDestinationAccountId());

        /*
         * Original:
         * DR source
         * CR destination
         *
         * Reversal:
         * DR destination
         * CR source
         * */

        this.ledgerService.postCustomerTransfer(reversalTransaction.getId(), destinationLedgerAccountId, sourceLedgerAccountId,
                originalTransaction.getAmount());
    }

    private Optional<TransactionView> getCompletedOrPendingTransaction(String idempotencyKey) {
        return this.transactionRepository.findByIdempotencyKey(idempotencyKey).map(this.transactionMapper::toView);
    }

    private Long getCustomerLedgerAccountId(Long accountId) {
        return this.ledgerService.getSubLedgerAccountIdByAccountId(accountId).orElseThrow(() -> {
            log.warn("No sub-ledger account mapped to account: {}", accountId);
            return new ApiResourceException("No sub-ledger account mapped to account", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
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
                                          TransactionType transactionType, BigDecimal amount, String description,
                                          String idempotencyKey) {
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(idempotencyKey);
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

    private TransactionDirection determineDirection(Transaction transaction, Long accountId) {
        return switch (transaction.getTransactionType()) {
            case DEPOSIT ->
                    transaction.getReversalOfTransactionId() == null ? TransactionDirection.CREDIT : TransactionDirection.DEBIT;
            case WITHDRAWAL ->
                    transaction.getReversalOfTransactionId() == null ? TransactionDirection.DEBIT : TransactionDirection.CREDIT;
            case TRANSFER -> {
                boolean isSource = transaction.getAccountId().equals(accountId);

                if (transaction.getReversalOfTransactionId() == null) {
                    yield isSource ? TransactionDirection.DEBIT : TransactionDirection.CREDIT;
                }

                yield isSource ? TransactionDirection.CREDIT : TransactionDirection.DEBIT;
            }
            default -> null;
        };
    }

    private AccountTransactionView toAccountTransactionView(Transaction transaction, Long accountId) {
        return new AccountTransactionView(transaction.getId(), transaction.getTransactionReference(),
                this.determineDirection(transaction, accountId),
                transaction.getTransactionType(), transaction.getCurrencyId(),
                transaction.getAmount(), transaction.getStatus(),
                transaction.getDescription(), transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
