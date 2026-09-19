package io.github.hossensyedriadh.cbs.ledger.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.command.CreateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.command.UpdateLedgerAccountCommand;
import io.github.hossensyedriadh.cbs.ledger.api.model.LedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerAccountService;
import io.github.hossensyedriadh.cbs.ledger.internal.domain.LedgerAccount;
import io.github.hossensyedriadh.cbs.ledger.internal.mapper.LedgerAccountMapper;
import io.github.hossensyedriadh.cbs.ledger.internal.repository.LedgerAccountRepository;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.product.api.service.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class LedgerAccountServiceImpl implements LedgerAccountService {
    private final LedgerAccountRepository ledgerAccountRepository;
    private final LedgerAccountMapper ledgerAccountMapper;
    private final CurrencyService currencyService;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public LedgerAccountServiceImpl(LedgerAccountRepository ledgerAccountRepository,
                                    LedgerAccountMapper ledgerAccountMapper, CurrencyService currencyService,
                                    HttpServletRequest httpServletRequest) {
        this.ledgerAccountRepository = ledgerAccountRepository;
        this.ledgerAccountMapper = ledgerAccountMapper;
        this.currencyService = currencyService;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Optional<LedgerAccountView> getLedgerAccount(Long id) {
        return this.ledgerAccountRepository.findById(id).map(this.ledgerAccountMapper::toView);
    }

    @Override
    public Optional<LedgerAccountView> getLedgerAccountByCode(String accountCode) {
        return this.ledgerAccountRepository.findByAccountCode(accountCode).map(this.ledgerAccountMapper::toView);
    }

    @Transactional
    @Override
    public LedgerAccountView createLedgerAccount(CreateLedgerAccountCommand command) {
        CurrencyView currency = this.currencyService.getCurrency(command.getCurrencyId())
                .orElseThrow(() -> {
                    log.warn("Currency not found with ID: {}", command.getCurrencyId());
                    return new ApiResourceException("Currency not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (!currency.isActive()) {
            log.warn("Current ID: {} is not active.", command.getCurrencyId());
            throw new ApiResourceException("Current is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        LedgerAccount account = this.ledgerAccountMapper.toEntity(command);
        LedgerAccount saved = this.ledgerAccountRepository.save(account);

        log.info("Created ledger account with code: {}", saved.getAccountCode());

        return this.ledgerAccountMapper.toView(saved);
    }

    @Transactional
    @Override
    public Optional<LedgerAccountView> updateLedgerAccount(Long id, UpdateLedgerAccountCommand command) {
        this.ledgerAccountRepository.findById(id).map(account -> {
            if (account.getActive() == Boolean.FALSE) {
                log.warn("Ledger account with ID: {} is not active", id);
                throw new ApiResourceException("Ledger account not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            this.ledgerAccountMapper.toPatchedEntity(command, account);
            LedgerAccount updated = this.ledgerAccountRepository.save(account);

            log.info("Updated ledger account with ID: {}", id);

            return this.ledgerAccountMapper.toView(updated);
        });

        log.warn("Ledger account with ID: {} not found", id);
        throw new ApiResourceException("Ledger account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
    }

    @Transactional
    @Override
    public void activateLedgerAccount(Long id) {
        this.ledgerAccountRepository.findById(id).ifPresentOrElse(account -> {
            if (account.getActive() == Boolean.TRUE) {
                log.warn("Ledger account with ID: {} is already active", id);
                throw new ApiResourceException("Ledger account is already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setActive(Boolean.TRUE);
            this.ledgerAccountRepository.save(account);

            log.info("Activated ledger account with ID: {}", id);
        }, () -> {
            log.warn("Ledger account with ID: {} not found", id);
            throw new ApiResourceException("Ledger account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateLedgerAccount(Long id) {
        this.ledgerAccountRepository.findById(id).ifPresentOrElse(account -> {
            if (account.getActive() == Boolean.FALSE) {
                log.warn("Ledger account with ID: {} is already inactive", id);
                throw new ApiResourceException("Ledger account is already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            account.setActive(Boolean.FALSE);
            this.ledgerAccountRepository.save(account);

            log.info("Deactivated ledger account with ID: {}", id);
        }, () -> {
            log.warn("Ledger account with ID: {} not found", id);
            throw new ApiResourceException("Ledger account not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });
    }
}
