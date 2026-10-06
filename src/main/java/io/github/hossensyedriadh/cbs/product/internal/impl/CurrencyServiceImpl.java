package io.github.hossensyedriadh.cbs.product.internal.impl;

import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.command.CreateCurrencyCommand;
import io.github.hossensyedriadh.cbs.product.api.event.currency.CurrencyActivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.currency.CurrencyCreatedEvent;
import io.github.hossensyedriadh.cbs.product.api.event.currency.CurrencyDeactivatedEvent;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.product.api.service.CurrencyService;
import io.github.hossensyedriadh.cbs.product.internal.domain.Currency;
import io.github.hossensyedriadh.cbs.product.internal.mapper.CurrencyMapper;
import io.github.hossensyedriadh.cbs.product.internal.repository.CurrencyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CurrencyMapper currencyMapper;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CurrencyServiceImpl(CurrencyRepository currencyRepository, CurrencyMapper currencyMapper,
                               ApplicationEventPublisher applicationEventPublisher, HttpServletRequest httpServletRequest) {
        this.currencyRepository = currencyRepository;
        this.currencyMapper = currencyMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<CurrencyView> getCurrencies() {
        return this.currencyRepository.findAll().stream().map(this.currencyMapper::toView).toList();
    }

    @Override
    public Optional<CurrencyView> getCurrency(Long id) {
        return this.currencyRepository.findById(id).map(this.currencyMapper::toView);
    }

    @Override
    public Optional<CurrencyView> getCurrencyByCode(String code) {
        return this.currencyRepository.findByCode(code).map(this.currencyMapper::toView);
    }

    @Transactional
    @Override
    public CurrencyView createCurrency(@Valid CreateCurrencyCommand command) {
        if (this.currencyRepository.existsByCode(command.getCode())) {
            log.warn("Currency with code {} already exists", command.getCode());
            throw new ApiResourceException("Currency already exists", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        Currency currency = this.currencyMapper.toEntity(command);
        Currency saved = this.currencyRepository.save(currency);

        this.applicationEventPublisher.publishEvent(new CurrencyCreatedEvent(saved.getId(), saved.getCode()));

        log.info("Created currency with code: {}", saved.getCode());

        return this.currencyMapper.toView(saved);
    }

    @Transactional
    @Override
    public void activateCurrency(Long id) {
        this.currencyRepository.findById(id).ifPresentOrElse(currency -> {
            if (currency.getActive() == Boolean.TRUE) {
                log.warn("Currency with ID: {} is already active", id);
                throw new ApiResourceException("Currency already active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            currency.setActive(true);
            this.currencyRepository.save(currency);

            this.applicationEventPublisher.publishEvent(new CurrencyActivatedEvent(currency.getId(), currency.getCode()));

            log.info("Activated currency with ID: {}", id);
        }, () -> {
            log.warn("Currency with id {} not found", id);
            throw new ApiResourceException("Currency not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });
    }

    @Transactional
    @Override
    public void deactivateCurrency(Long id) {
        this.currencyRepository.findById(id).ifPresentOrElse(currency -> {
            if (currency.getActive() == Boolean.FALSE) {
                log.warn("Currency with ID: {} is already inactive", id);
                throw new ApiResourceException("Currency already inactive", HttpStatus.BAD_REQUEST, this.httpServletRequest);
            }

            currency.setActive(false);
            this.currencyRepository.save(currency);

            this.applicationEventPublisher.publishEvent(new CurrencyDeactivatedEvent(currency.getId(), currency.getCode()));

            log.info("Deactivated currency with ID: {}", id);
        }, () -> {
            log.warn("Currency with id {} not found", id);
            throw new ApiResourceException("Currency not found", HttpStatus.NOT_FOUND, this.httpServletRequest);
        });
    }
}
