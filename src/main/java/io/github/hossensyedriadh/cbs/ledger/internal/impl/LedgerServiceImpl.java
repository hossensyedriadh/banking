package io.github.hossensyedriadh.cbs.ledger.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.LedgerService;
import io.github.hossensyedriadh.cbs.ledger.api.enumerator.JournalEntryType;
import io.github.hossensyedriadh.cbs.ledger.api.model.JournalEntryLine;
import io.github.hossensyedriadh.cbs.ledger.api.model.PostJournalEntryCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.JournalEntry;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.SubLedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.mapper.SubLedgerAccountMapper;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.JournalEntryRepository;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.LedgerAccountRepository;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.ProductLedgerAccountRepository;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.SubLedgerAccountRepository;
import io.github.hossensyedriadh.cbs.ledger.internal.util.SystemLedgerAccountCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional(readOnly = true)
public class LedgerServiceImpl implements LedgerService {
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountRepository ledgerAccountRepository;
    private final SubLedgerAccountRepository subLedgerAccountRepository;
    private final ProductLedgerAccountRepository productLedgerAccountRepository;
    private final SubLedgerAccountMapper subLedgerAccountMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public LedgerServiceImpl(JournalEntryRepository journalEntryRepository, LedgerAccountRepository ledgerAccountRepository,
                             SubLedgerAccountRepository subLedgerAccountRepository, ProductLedgerAccountRepository productLedgerAccountRepository,
                             SubLedgerAccountMapper subLedgerAccountMapper, HttpServletRequest httpServletRequest) {
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.subLedgerAccountRepository = subLedgerAccountRepository;
        this.productLedgerAccountRepository = productLedgerAccountRepository;
        this.subLedgerAccountMapper = subLedgerAccountMapper;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Optional<Long> getLedgerAccountByAccountId(Long accountId) {
        return this.subLedgerAccountRepository.findLedgerAccountIdByAccountId(accountId);
    }

    @Override
    public Optional<Long> getSubLedgerAccountIdByAccountId(Long accountId) {
        return this.subLedgerAccountRepository.findSubLedgerAccountIdByAccountId(accountId);
    }

    @Override
    public Optional<SubLedgerAccountView> getSubLedgerAccountByAccountId(Long accountId) {
        return this.subLedgerAccountRepository.findSubLedgerAccountByAccountId(accountId).map(this.subLedgerAccountMapper::toView);
    }

    @Transactional
    @Override
    public void createSubLedgerAccount(Long accountId, Long accountProductId) {
        if (this.subLedgerAccountRepository.existsByAccountId(accountId)) {
            log.warn("Sub ledger account with ID: {} already exists", accountId);
            throw new ApiResourceException("Subledger account already exists for account", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        Long ledgerAccountId = this.productLedgerAccountRepository.findLedgerAccountIdByProductId(accountProductId).orElseThrow(() -> {
            log.warn("No ledger account configured for product: {}", accountProductId);
            return new ApiResourceException("No ledger account configured for product", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        LedgerAccount ledgerAccount = this.ledgerAccountRepository.findById(ledgerAccountId).orElseThrow(() -> {
            log.warn("Ledger account with ID: {} does not exist", ledgerAccountId);
            return new ApiResourceException("Ledger account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (!ledgerAccount.getActive()) {
            log.warn("Ledger account with ID: {} is inactive", ledgerAccountId);
            throw new ApiResourceException("Ledger account is inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        if (this.subLedgerAccountRepository.existsByLedgerAccountId(ledgerAccountId)) {
            log.warn("Ledger account with ID: {} already mapped to another banking account", ledgerAccountId);
            throw new ApiResourceException("Ledger account is already mapped to another banking account", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        SubLedgerAccount subLedgerAccount = new SubLedgerAccount();
        subLedgerAccount.setAccountId(accountId);
        subLedgerAccount.setLedgerAccount(ledgerAccount);

        this.subLedgerAccountRepository.save(subLedgerAccount);
    }

    private void postJournalEntry(PostJournalEntryCommand command) {
        BigDecimal totalDebit = command.getEntries().stream()
                .filter(line -> line.getEntryType() == JournalEntryType.DEBIT)
                .map(JournalEntryLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = command.getEntries().stream()
                .filter(line -> line.getEntryType() == JournalEntryType.CREDIT)
                .map(JournalEntryLine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            log.warn("Journal entry is not balanced for transaction: {}", command.getTransactionId());
            throw new ApiResourceException("Journal entry is not balanced", HttpStatus.INTERNAL_SERVER_ERROR, this.httpServletRequest);
        }

        Set<Long> ledgerAccountIds = command.getEntries().stream().map(JournalEntryLine::getLedgerAccountId).collect(Collectors.toSet());

        Map<Long, LedgerAccount> ledgerAccounts = this.ledgerAccountRepository.findAllById(ledgerAccountIds).stream()
                .collect(Collectors.toMap(LedgerAccount::getId, Function.identity()));

        List<JournalEntry> entries = command.getEntries().stream()
                .map(line -> {
                    LedgerAccount ledgerAccount = ledgerAccounts.get(line.getLedgerAccountId());

                    if (ledgerAccount == null) {
                        log.warn("Ledger account with ID: {} not found for transaction: {}", line.getLedgerAccountId(), command.getTransactionId());
                        throw new ApiResourceException("Ledger account not found for transaction", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                    }

                    JournalEntry entry = new JournalEntry();
                    entry.setTransactionId(command.getTransactionId());
                    entry.setAmount(line.getAmount());
                    entry.setEntryType(line.getEntryType());
                    entry.setLedgerAccount(ledgerAccount);

                    return entry;
                }).toList();

        this.journalEntryRepository.saveAll(entries);

        log.info("{} journal entries saved successfully for transaction: {}", entries.size(), command.getTransactionId());
    }

    @Transactional
    @Override
    public void postDeposit(Long transactionId, Long customerLedgerAccountId, BigDecimal amount) {
        Long cashLedgerAccountId = this.getCashLedgerAccountId();
        Long customerDepositsLedgerAccountId = this.getCustomerDepositLedgerAccountId();

        this.postJournalEntry(new PostJournalEntryCommand(transactionId,
                List.of(
                        new JournalEntryLine(cashLedgerAccountId, JournalEntryType.DEBIT, amount),
                        new JournalEntryLine(customerDepositsLedgerAccountId, JournalEntryType.CREDIT, amount)
                )
        ));

        this.adjustGLAccountBalance(cashLedgerAccountId, amount);
        this.adjustGLAccountBalance(customerDepositsLedgerAccountId, amount);
        this.adjustSubLedgerAccountBalance(customerLedgerAccountId, amount);
    }

    @Transactional
    @Override
    public void postWithdrawal(Long transactionId, Long customerLedgerAccountId, BigDecimal amount) {
        Long cashLedgerAccountId = this.getCashLedgerAccountId();
        Long customerDepositsLedgerAccountId = this.getCustomerDepositLedgerAccountId();

        this.postJournalEntry(new PostJournalEntryCommand(transactionId,
                List.of(
                        new JournalEntryLine(customerDepositsLedgerAccountId, JournalEntryType.DEBIT, amount),
                        new JournalEntryLine(cashLedgerAccountId, JournalEntryType.CREDIT, amount)
                )
        ));

        this.adjustGLAccountBalance(cashLedgerAccountId, amount.negate());
        this.adjustGLAccountBalance(customerDepositsLedgerAccountId, amount.negate());
        this.adjustSubLedgerAccountBalance(customerLedgerAccountId, amount.negate());
    }

    @Transactional
    @Override
    public void postCustomerTransfer(Long transactionId, Long sourceLedgerAccountId, Long destinationLedgerAccountId, BigDecimal fee, BigDecimal amount) {
        Long feeIncomeLedgerAccountId = this.getFeeIncomeLedgerAccountId();
        Long customerDepositsLedgerAccountId = this.getCustomerDepositLedgerAccountId();

        this.postJournalEntry(new PostJournalEntryCommand(transactionId, List.of(
                new JournalEntryLine(customerDepositsLedgerAccountId, JournalEntryType.DEBIT, fee),
                new JournalEntryLine(feeIncomeLedgerAccountId, JournalEntryType.CREDIT, fee)
        )));

        this.adjustGLAccountBalance(customerDepositsLedgerAccountId, fee.negate());
        this.adjustGLAccountBalance(feeIncomeLedgerAccountId, fee);
        this.adjustSubLedgerAccountBalance(sourceLedgerAccountId, amount.add(fee).negate());
        this.adjustSubLedgerAccountBalance(destinationLedgerAccountId, amount);
    }

    @Transactional
    @Override
    public void postInterest(Long accrualId, Long customerLedgerAccountId, BigDecimal amount) {
        Long interestExpenseLedgerAccountId = this.getInterestExpenseLedgerAccountId();
        Long customerDepositsLedgerAccountId = this.getCustomerDepositLedgerAccountId();

        this.postJournalEntry(new PostJournalEntryCommand(accrualId, List.of(
                new JournalEntryLine(interestExpenseLedgerAccountId, JournalEntryType.DEBIT, amount),
                new JournalEntryLine(customerDepositsLedgerAccountId, JournalEntryType.CREDIT, amount))
        ));

        this.adjustGLAccountBalance(interestExpenseLedgerAccountId, amount.negate());
        this.adjustGLAccountBalance(customerDepositsLedgerAccountId, amount);
        this.adjustSubLedgerAccountBalance(customerLedgerAccountId, amount);
    }

    private Long getCashLedgerAccountId() {
        return this.ledgerAccountRepository.findByAccountCode(SystemLedgerAccountCodes.CASH).map(LedgerAccount::getId).orElseThrow(() -> {
            log.warn("Cash ledger account not configured");
            return new ApiResourceException("Cash ledger account not configured", HttpStatus.INTERNAL_SERVER_ERROR, this.httpServletRequest);
        });
    }

    private Long getCustomerDepositLedgerAccountId() {
        return this.ledgerAccountRepository.findByAccountCode(SystemLedgerAccountCodes.CUSTOMER_DEPOSITS).map(LedgerAccount::getId).orElseThrow(() -> {
            log.warn("Customer deposits ledger account not configured");
            return new ApiResourceException("Customer deposits ledger account not configured", HttpStatus.INTERNAL_SERVER_ERROR, this.httpServletRequest);
        });
    }

    private Long getFeeIncomeLedgerAccountId() {
        return this.ledgerAccountRepository.findByAccountCode(SystemLedgerAccountCodes.FEE_INCOME).map(LedgerAccount::getId).orElseThrow(() -> {
            log.warn("Fee income ledger account not configured");
            return new ApiResourceException("Fee income account not configured", HttpStatus.INTERNAL_SERVER_ERROR, this.httpServletRequest);
        });
    }

    private Long getInterestExpenseLedgerAccountId() {
        return this.ledgerAccountRepository.findByAccountCode(SystemLedgerAccountCodes.INTEREST_EXPENSE).map(LedgerAccount::getId).orElseThrow(() -> {
            log.warn("Interest expense ledger account not configured");
            return new ApiResourceException("Interest expense account not configured", HttpStatus.INTERNAL_SERVER_ERROR, this.httpServletRequest);
        });
    }

    private void adjustGLAccountBalance(Long ledgerAccountId, BigDecimal delta) {
        LedgerAccount ledgerAccount = this.ledgerAccountRepository.findByIdForUpdate(ledgerAccountId)
                .orElseThrow(() -> {
                    log.warn("GL account not found with ID: {}", ledgerAccountId);
                    return new ApiResourceException("GL account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        ledgerAccount.setBalance(ledgerAccount.getBalance().add(delta));
    }

    private void adjustSubLedgerAccountBalance(Long subLedgerAccountId, BigDecimal delta) {
        SubLedgerAccount subLedgerAccount = this.subLedgerAccountRepository.findByIdForUpdate(subLedgerAccountId)
                .orElseThrow(() -> {
                    log.warn("SubLedger account not found with ID: {}", subLedgerAccountId);
                    return new ApiResourceException("SubLedger account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        subLedgerAccount.setBalance(subLedgerAccount.getBalance().add(delta));
    }
}
