package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.command.CloseAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.command.CreateAccountCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHolderType;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.account.internal.domain.Account;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolder;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolderId;
import io.github.hossensyedriadh.cbs.account.internal.mapper.AccountMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHoldRepository;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHolderRepository;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRepository;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRestrictionRepository;
import io.github.hossensyedriadh.cbs.branch.api.service.BranchService;
import io.github.hossensyedriadh.cbs.customer.api.service.CustomerService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerService;
import io.github.hossensyedriadh.cbs.product.api.enumerator.AccountProductStatus;
import io.github.hossensyedriadh.cbs.product.api.service.AccountProductService;
import io.github.hossensyedriadh.cbs.product.api.service.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountHolderRepository accountHolderRepository;
    private final AccountHoldRepository accountHoldRepository;
    private final AccountRestrictionRepository accountRestrictionRepository;
    private final AccountMapper accountMapper;
    private final CustomerService customerService;
    private final AccountProductService accountProductService;
    private final BranchService branchService;
    private final CurrencyService currencyService;
    private final LedgerService ledgerService;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, AccountHolderRepository accountHolderRepository,
                              AccountHoldRepository accountHoldRepository, AccountRestrictionRepository accountRestrictionRepository,
                              AccountMapper accountMapper, CustomerService customerService,
                              AccountProductService accountProductService, BranchService branchService,
                              CurrencyService currencyService, LedgerService ledgerService,
                              HttpServletRequest httpServletRequest) {
        this.accountRepository = accountRepository;
        this.accountHolderRepository = accountHolderRepository;
        this.accountHoldRepository = accountHoldRepository;
        this.accountRestrictionRepository = accountRestrictionRepository;
        this.accountMapper = accountMapper;
        this.customerService = customerService;
        this.accountProductService = accountProductService;
        this.branchService = branchService;
        this.currencyService = currencyService;
        this.ledgerService = ledgerService;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<AccountView> getAccountByCustomerId(Long customerId) {
        return this.accountRepository.findAllByCustomerId(customerId).stream().map(this.accountMapper::toView).toList();
    }

    @Override
    public List<AccountView> getActiveAccounts() {
        return this.accountRepository.findAllByStatus(AccountStatus.ACTIVE).stream().map(this.accountMapper::toView).toList();
    }

    @Override
    public Optional<AccountView> getAccount(Long id) {
        return this.accountRepository.findById(id).map(this.accountMapper::toView);
    }

    @Override
    public Optional<AccountView> getAccountByNumber(String accountNumber) {
        return this.accountRepository.findByAccountNumber(accountNumber).map(this.accountMapper::toView);
    }

    @Transactional
    @Override
    public Optional<AccountView> getAccountForUpdate(Long id) {
        return this.accountRepository.findByIdForUpdate(id).map(this.accountMapper::toView);
    }

    @Transactional
    @Override
    public AccountView createAccount(@Valid CreateAccountCommand command) {
        this.validateCustomers(command.getCustomerIds());

        this.accountProductService.getProduct(command.getProductId()).ifPresentOrElse(product -> {
            if (product.getStatus() != AccountProductStatus.ACTIVE) {
                log.warn("Account product ID: {} is not active", product.getId());
                throw new ApiResourceException("Account product is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
        }, () -> {
            log.warn("Account product not found with id {}", command.getProductId());
            throw new ApiResourceException("Account product not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        this.branchService.getBranch(command.getBranchId()).ifPresentOrElse(branch -> {
            if (!branch.isActive()) {
                log.warn("Account branch ID: {} is not active", branch.getId());
                throw new ApiResourceException("Account branch not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
        }, () -> {

            log.warn("Branch not found with id {}", command.getBranchId());
            throw new ApiResourceException("Branch not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        this.currencyService.getCurrency(command.getCurrencyId()).ifPresentOrElse(currency -> {
            if (!currency.isActive()) {
                log.warn("Currency ID: {} is not active", currency.getId());
                throw new ApiResourceException("Currency not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }
        }, () -> {
            log.warn("Currency not found with id {}", command.getCurrencyId());
            throw new ApiResourceException("Currency not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        Account account = this.accountMapper.toEntity(command);
        Account saved = this.accountRepository.save(account);

        this.ledgerService.createSubLedgerAccount(saved.getId(), command.getProductId());

        List<AccountHolder> holders = this.createAccountHolders(saved, command.getCustomerIds());

        this.accountHolderRepository.saveAll(holders);

        log.info("Created account with ID: {}", saved.getId());

        return this.accountMapper.toView(saved);
    }

    @Transactional
    @Override
    public void activateAccount(Long id) {
        this.accountRepository.findByIdForUpdate(id).ifPresentOrElse(account -> {
            if (account.getStatus().equals(AccountStatus.ACTIVE) || account.getStatus().equals(AccountStatus.CLOSED)) {
                log.warn("Account with ID: {} already {}", id, account.getStatus().toString().toLowerCase());
                throw new ApiResourceException(String.format("Account already %s", account.getStatus().toString().toLowerCase()),
                        HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setStatus(AccountStatus.ACTIVE);
            Account saved = this.accountRepository.save(account);

            log.info("Account with ID: {} successfully activated", saved.getId());

        }, () -> {
            log.warn("No account found with ID: {}", id);
            throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateAccount(Long id) {
        this.accountRepository.findByIdForUpdate(id).ifPresentOrElse(account -> {
            if (account.getStatus().equals(AccountStatus.BLOCKED) || account.getStatus().equals(AccountStatus.CLOSED)) {
                log.warn("Account with ID: {} already {}", id, account.getStatus().toString().toLowerCase());
                throw new ApiResourceException(String.format("Account already %s", account.getStatus().toString().toLowerCase()),
                        HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setStatus(AccountStatus.BLOCKED);
            Account saved = this.accountRepository.save(account);

            log.info("Account with ID: {} successfully deactivated", saved.getId());
        }, () -> {
            log.warn("No account found with ID: {}", id);
            throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public AccountView closeAccount(@Valid CloseAccountCommand command) {
        Account account = this.accountRepository.findByIdForUpdate(command.getAccountId()).orElseThrow(() -> {
            log.warn("No account found with ID: {}", command.getAccountId());
            return new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account ID: {} is not active", command.getAccountId());
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        BigDecimal ledgerBalance = this.ledgerService.getSubLedgerAccountByAccountId(account.getId())
                .map(SubLedgerAccountView::getBalance).orElse(BigDecimal.ZERO);

        if (ledgerBalance.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Account ID: {} is not at zero balance", command.getAccountId());
            throw new ApiResourceException("Account balance must be zero before closure", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        boolean hasActiveHolds = this.accountHoldRepository.existsByAccountIdAndStatus(account.getId(), AccountHoldStatus.ACTIVE);

        if (hasActiveHolds) {
            log.warn("Account with ID: {} has active holds", command.getAccountId());
            throw new ApiResourceException("Account has active holds", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        boolean hasActiveRestrictions = this.accountRestrictionRepository.existsByAccountIdAndStatus(account.getId(), AccountRestrictionStatus.ACTIVE);

        if (hasActiveRestrictions) {
            log.warn("Account with ID: {} has active restrictions", command.getAccountId());
            throw new ApiResourceException("Account has active restrictions", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setClosedAt(OffsetDateTime.now());
        account.setClosureReason(command.getReason());

        Account closed = this.accountRepository.save(account);

        log.info("Account with ID: {} successfully closed", closed.getId());

        return this.accountMapper.toView(closed);
    }

    private void validateCustomers(List<Long> customerIds) {
        for (Long customerId : customerIds) {
            this.customerService.getCustomer(customerId).orElseThrow(() -> {
                log.warn("Customer not found with ID: {}", customerId);
                return new ApiResourceException("Customer not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            });
        }
    }

    private List<AccountHolder> createAccountHolders(Account account, List<Long> customerIds) {
        List<AccountHolder> holders = new ArrayList<>();

        for (int i = 0; i < customerIds.size(); i++) {
            Long customerId = customerIds.get(i);

            AccountHolder holder = new AccountHolder();
            AccountHolderId holderId = new AccountHolderId();

            holderId.setAccountId(account.getId());
            holderId.setCustomerId(customerId);

            holder.setId(holderId);
            holder.setAccount(account);

            holder.setHolderType(i == 0 ? AccountHolderType.PRIMARY : AccountHolderType.JOINT);

            holders.add(holder);
        }

        return holders;
    }
}
