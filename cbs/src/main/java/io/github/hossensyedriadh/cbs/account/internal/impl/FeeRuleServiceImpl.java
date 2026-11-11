package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.command.CreateFeeRuleCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.event.FeeRuleActivatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.FeeRuleCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.FeeRuleDeactivatedEvent;
import io.github.hossensyedriadh.cbs.account.api.model.FeeRuleView;
import io.github.hossensyedriadh.cbs.account.api.service.FeeRuleService;
import io.github.hossensyedriadh.cbs.account.internal.domain.FeeRule;
import io.github.hossensyedriadh.cbs.account.internal.mapper.FeeRuleMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.FeeRuleRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.product.api.model.CurrencyView;
import io.github.hossensyedriadh.cbs.product.api.service.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional(readOnly = true)
public class FeeRuleServiceImpl implements FeeRuleService {
    private final FeeRuleRepository feeRuleRepository;
    private final CurrencyService currencyService;
    private final FeeRuleMapper feeRuleMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public FeeRuleServiceImpl(FeeRuleRepository feeRuleRepository, CurrencyService currencyService,
                              FeeRuleMapper feeRuleMapper, ApplicationEventPublisher applicationEventPublisher,
                              HttpServletRequest httpServletRequest) {
        this.feeRuleRepository = feeRuleRepository;
        this.currencyService = currencyService;
        this.feeRuleMapper = feeRuleMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public Page<FeeRuleView> getFeeRules(FeeRuleStatus status, Pageable pageable) {
        return this.feeRuleRepository.findAllByStatus(status, pageable).map(this.feeRuleMapper::toView);
    }

    @Override
    public List<FeeRuleView> getFeeRules(FeeRuleStatus status) {
        return this.feeRuleRepository.findAllByStatus(status).stream()
                .map(this.feeRuleMapper::toView)
                .toList();
    }

    @Override
    public Optional<FeeRuleView> getFeeRule(Long id) {
        return this.feeRuleRepository.findById(id).map(this.feeRuleMapper::toView);
    }

    @Override
    public Optional<FeeRuleView> getFeeRule(String feeCode, FeeRuleStatus status) {
        return this.feeRuleRepository.findByFeeCodeAndStatus(feeCode, status).map(this.feeRuleMapper::toView);
    }

    @Transactional
    @Override
    public FeeRuleView createFeeRule(@Valid CreateFeeRuleCommand command) {
        if (this.feeRuleRepository.existsByFeeCode(command.getFeeCode())) {
            log.warn("Fee code {} already exists", command.getFeeCode());
            throw new ApiResourceException("Fee code already exists", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        CurrencyView currencyView = this.currencyService.getCurrency(command.getCurrencyId())
                .orElseThrow(() -> {
                    log.warn("Currency id {} not found", command.getCurrencyId());
                    return new ApiResourceException("Currency not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
                });

        if (!currencyView.isActive()) {
            log.warn("Currency id {} is not active", command.getCurrencyId());
            throw new ApiResourceException("Currency is not active", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        }

        FeeRule rule = this.feeRuleMapper.toEntity(command);
        FeeRule saved = this.feeRuleRepository.save(rule);

        this.applicationEventPublisher.publishEvent(new FeeRuleCreatedEvent(saved.getId(), saved.getFeeCode()));

        log.info("Created fee rule with code: {}", rule.getFeeCode());

        return this.feeRuleMapper.toView(saved);
    }

    @Transactional
    @Override
    public void activateFeeRule(Long id) {
        FeeRule feeRule = this.feeRuleRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Fee rule with id {} not found", id);
            return new ApiResourceException("Fee rule not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        feeRule.setStatus(FeeRuleStatus.ACTIVE);
        this.feeRuleRepository.save(feeRule);

        this.applicationEventPublisher.publishEvent(new FeeRuleActivatedEvent(feeRule.getId(), feeRule.getFeeCode()));

        log.info("Activated fee rule with id: {}", feeRule.getId());
    }

    @Transactional
    @Override
    public void deactivateFeeRule(Long id) {
        FeeRule feeRule = this.feeRuleRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Fee rule with id {} not found", id);
            return new ApiResourceException("Fee rule not found", HttpStatus.BAD_REQUEST, this.httpServletRequest);
        });

        feeRule.setStatus(FeeRuleStatus.INACTIVE);
        this.feeRuleRepository.save(feeRule);

        this.applicationEventPublisher.publishEvent(new FeeRuleDeactivatedEvent(feeRule.getId(), feeRule.getFeeCode()));

        log.info("Deactivated fee rule with id: {}", feeRule.getId());
    }
}
