package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.AccountService;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHolderType;
import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.model.CreateAccountCommand;
import io.github.hossensyedriadh.cbs.account.internal.domain.Account;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolder;
import io.github.hossensyedriadh.cbs.account.internal.domain.AccountHolderId;
import io.github.hossensyedriadh.cbs.account.internal.mapper.AccountMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountHolderRepository;
import io.github.hossensyedriadh.cbs.account.internal.repository.AccountRepository;
import io.github.hossensyedriadh.cbs.branch.api.BranchService;
import io.github.hossensyedriadh.cbs.customer.api.CustomerService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.LedgerService;
import io.github.hossensyedriadh.cbs.product.api.AccountProductService;
import io.github.hossensyedriadh.cbs.product.api.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountHolderRepository accountHolderRepository;
    private final AccountMapper accountMapper;
    private final CustomerService customerService;
    private final AccountProductService accountProductService;
    private final BranchService branchService;
    private final CurrencyService currencyService;
    private final LedgerService ledgerService;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, AccountHolderRepository accountHolderRepository,
                              AccountMapper accountMapper, CustomerService customerService,
                              AccountProductService accountProductService, BranchService branchService,
                              CurrencyService currencyService, LedgerService ledgerService,
                              HttpServletRequest httpServletRequest) {
        this.accountRepository = accountRepository;
        this.accountHolderRepository = accountHolderRepository;
        this.accountMapper = accountMapper;
        this.customerService = customerService;
        this.accountProductService = accountProductService;
        this.branchService = branchService;
        this.currencyService = currencyService;
        this.ledgerService = ledgerService;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Optional<AccountView> getAccount(Long id) {
        return this.accountRepository.findById(id).map(this.accountMapper::toView);
    }

    @Override
    public Optional<AccountView> getAccountByNumber(String accountNumber) {
        return this.accountRepository.findByAccountNumber(accountNumber).map(this.accountMapper::toView);
    }

    @Override
    public List<AccountView> getAccountByCustomerId(Long customerId) {
        return this.accountRepository.findAllByCustomerId(customerId).stream().map(this.accountMapper::toView).toList();
    }

    @Transactional
    @Override
    public AccountView createAccount(CreateAccountCommand command) {
        this.validateCustomers(command.getCustomerIds());

        this.accountProductService.getProduct(command.getProductId()).ifPresentOrElse(product -> {
            if (!product.isActive()) {
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
    public void blockAccount(Long id) {
        this.accountRepository.findById(id).ifPresentOrElse(account -> {
            if (account.getStatus().equals(AccountStatus.BLOCKED) || account.getStatus().equals(AccountStatus.CLOSED)) {
                log.warn("Account with ID: {} already {}", id, account.getStatus().toString().toLowerCase());
                throw new ApiResourceException(String.format("Account already %s", account.getStatus().toString().toLowerCase()),
                        HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setStatus(AccountStatus.BLOCKED);
            Account saved = this.accountRepository.save(account);

            log.info("Account with ID: {} successfully blocked", saved.getId());
        }, () -> {
            log.warn("No account found with ID: {}", id);
            throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void unblockAccount(Long id) {
        this.accountRepository.findById(id).ifPresentOrElse(account -> {
            if (account.getStatus().equals(AccountStatus.ACTIVE) || account.getStatus().equals(AccountStatus.CLOSED)) {
                log.warn("Account with ID: {} already {}", id, account.getStatus().toString().toLowerCase());
                throw new ApiResourceException(String.format("Account already %s", account.getStatus().toString().toLowerCase()),
                        HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setStatus(AccountStatus.ACTIVE);
            Account saved = this.accountRepository.save(account);

            log.info("Account with ID: {} successfully unblocked", saved.getId());

        }, () -> {
            log.warn("No account found with ID: {}", id);
            throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void closeAccount(Long id) {
        this.accountRepository.findById(id).ifPresentOrElse(account -> {
            if (account.getStatus().equals(AccountStatus.CLOSED)) {
                log.warn("Account with ID: {} already closed", id);
                throw new ApiResourceException("Account already closed", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setStatus(AccountStatus.CLOSED);
            Account saved = this.accountRepository.save(account);

            log.info("Account with ID: {} successfully closed", saved.getId());
        }, () -> {
            log.warn("No account found with ID: {}", id);
            throw new ApiResourceException("Account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
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
