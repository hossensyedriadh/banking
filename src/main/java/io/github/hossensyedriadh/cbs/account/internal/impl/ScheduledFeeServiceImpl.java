package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.command.ScheduleFeeCommand;
import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeCancelledEvent;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeCreatedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeProcessedEvent;
import io.github.hossensyedriadh.cbs.account.api.event.ScheduledFeeWaivedEvent;
import io.github.hossensyedriadh.cbs.account.api.model.FeeRuleView;
import io.github.hossensyedriadh.cbs.account.api.model.ScheduledFeeView;
import io.github.hossensyedriadh.cbs.account.api.service.FeeRuleService;
import io.github.hossensyedriadh.cbs.account.api.service.ScheduledFeeService;
import io.github.hossensyedriadh.cbs.account.internal.domain.ScheduledFee;
import io.github.hossensyedriadh.cbs.account.internal.mapper.ScheduledFeeMapper;
import io.github.hossensyedriadh.cbs.account.internal.repository.ScheduledFeeRepository;
import io.github.hossensyedriadh.cbs.account.internal.service.ScheduledFeeProcessor;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
public class ScheduledFeeServiceImpl implements ScheduledFeeService {
    private final ScheduledFeeRepository scheduledFeeRepository;
    private final ScheduledFeeProcessor scheduledFeeProcessor;
    private final ScheduledFeeMapper scheduledFeeMapper;
    private final FeeRuleService feeRuleService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${cbs.timezone}")
    private ZoneId cbsTimezone;

    @Value("${cbs.scheduled-fees.stale-processing-minutes:30}")
    private long staleProcessingMinutes;

    @Autowired
    public ScheduledFeeServiceImpl(ScheduledFeeRepository scheduledFeeRepository, ScheduledFeeProcessor scheduledFeeProcessor,
                                   ScheduledFeeMapper scheduledFeeMapper, FeeRuleService feeRuleService,
                                   ApplicationEventPublisher applicationEventPublisher) {
        this.scheduledFeeRepository = scheduledFeeRepository;
        this.scheduledFeeProcessor = scheduledFeeProcessor;
        this.scheduledFeeMapper = scheduledFeeMapper;
        this.feeRuleService = feeRuleService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ScheduledFeeView> getScheduledFees(@Nullable ScheduledFeeStatus status, Pageable pageable) {
        if (status == null) {
            return this.scheduledFeeRepository.findAll(pageable).map(this.scheduledFeeMapper::toView);
        }

        return this.scheduledFeeRepository.findAllByStatus(status, pageable).map(this.scheduledFeeMapper::toView);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ScheduledFeeView> getScheduledFees(Long accountId, ScheduledFeeStatus status, Pageable pageable) {
        if (status == null) {
            return this.scheduledFeeRepository.findAllByAccountId(accountId, pageable).map(this.scheduledFeeMapper::toView);
        }

        return this.scheduledFeeRepository.findAllByAccountIdAndStatus(accountId, status, pageable).map(this.scheduledFeeMapper::toView);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ScheduledFeeView> getScheduledFee(Long id) {
        return this.scheduledFeeRepository.findById(id).map(this.scheduledFeeMapper::toView);
    }

    @Override
    public Optional<ScheduledFeeView> getScheduledFee(Long accountId, Long id) {
        return this.scheduledFeeRepository.findByIdAndAccountId(id, accountId).map(this.scheduledFeeMapper::toView);
    }

    @Transactional
    @Override
    public ScheduledFeeView schedule(@Valid ScheduleFeeCommand command) {
        FeeRuleView feeRule = this.feeRuleService.getFeeRule(command.getFeeRuleId())
                .orElseThrow(() -> {
                    log.warn("Fee rule not found for id: {}", command.getFeeRuleId());
                    return new ApiResourceException("Fee rule not found", HttpStatus.NOT_FOUND);
                });

        if (feeRule.getStatus() != FeeRuleStatus.ACTIVE) {
            log.warn("Fee rule is not active for id: {}", command.getFeeRuleId());
            throw new ApiResourceException("Fee rule is not active", HttpStatus.BAD_REQUEST);
        }

        if (command.getCardId() == null) {
            boolean exists = this.scheduledFeeRepository.existsByFeeRuleIdAndAccountIdAndScheduledDateAndCardIdIsNull(command.getFeeRuleId(),
                    command.getAccountId(), command.getScheduledDate());

            if (exists) {
                log.warn("Scheduled fee already exists for fee rule id: {}, account id: {}, and scheduled date: {}", command.getFeeRuleId(), command.getAccountId(), command.getScheduledDate());
                throw new ApiResourceException("Scheduled fee already exists", HttpStatus.CONFLICT);
            }
        } else {
            boolean exists = this.scheduledFeeRepository.existsByFeeRuleIdAndCardIdAndScheduledDate(command.getFeeRuleId(), command.getCardId(), command.getScheduledDate());

            if (exists) {
                log.warn("Scheduled fee already exists for fee rule id: {}, card id: {}, and scheduled date: {}", command.getFeeRuleId(), command.getCardId(), command.getScheduledDate());
                throw new ApiResourceException("Scheduled fee already exists", HttpStatus.CONFLICT);
            }
        }

        ScheduledFee fee = new ScheduledFee();
        fee.setFeeRuleId(command.getFeeRuleId());
        fee.setAccountId(command.getAccountId());
        fee.setCardId(command.getCardId());
        fee.setScheduledDate(command.getScheduledDate());
        fee.setAmount(command.getAmount());
        fee.setStatus(ScheduledFeeStatus.PENDING);

        ScheduledFee saved = this.scheduledFeeRepository.save(fee);

        this.applicationEventPublisher.publishEvent(new ScheduledFeeCreatedEvent(saved.getId(), saved.getScheduledDate()));

        log.info("Scheduled fee created with id: {}", saved.getId());

        return this.scheduledFeeMapper.toView(saved);
    }

    @Override
    public void processDueFees(LocalDate businessDate) {
        OffsetDateTime now = OffsetDateTime.now(this.cbsTimezone);
        OffsetDateTime cutoff = now.minusMinutes(this.staleProcessingMinutes);

        int recovered = this.scheduledFeeRepository.recoverStaleProcessingFees(ScheduledFeeStatus.PROCESSING, ScheduledFeeStatus.FAILED,
                cutoff, "Processing claim expired; eligible for retry");

        if (recovered > 0) {
            log.warn("Recovered {} stale scheduled-fee processing claims", recovered);
        }

        List<ScheduledFee> dueFees = this.scheduledFeeRepository.findAllByScheduledDateLessThanEqualAndStatusInOrderByScheduledDateAscIdAsc(
                businessDate, List.of(ScheduledFeeStatus.PENDING, ScheduledFeeStatus.FAILED));

        for (ScheduledFee fee : dueFees) {
            try {
                int claimed = this.scheduledFeeRepository.claimScheduledFee(fee.getId(), ScheduledFeeStatus.PROCESSING,
                        OffsetDateTime.now(this.cbsTimezone), List.of(ScheduledFeeStatus.PENDING, ScheduledFeeStatus.FAILED));

                if (claimed == 0) {
                    continue;
                }

                this.scheduledFeeProcessor.processFee(fee.getId());
                this.applicationEventPublisher.publishEvent(new ScheduledFeeProcessedEvent(fee.getId(), fee.getScheduledDate()));
            } catch (Exception e) {
                log.error("Failed to process scheduled fee {}", fee.getId(), e);
            }
        }
    }

    @Transactional
    @Override
    public void cancelScheduledFee(Long id) {
        ScheduledFee fee = this.scheduledFeeRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Scheduled fee not found with id: {}", id);
            return new ApiResourceException("Scheduled fee not found", HttpStatus.NOT_FOUND);
        });

        if (fee.getStatus() != ScheduledFeeStatus.PENDING && fee.getStatus() != ScheduledFeeStatus.FAILED) {
            log.warn("Scheduled fee: {} cannot be cancelled", fee.getId());
            throw new ApiResourceException("Only pending or failed scheduled fees can be cancelled", HttpStatus.BAD_REQUEST);
        }

        fee.setStatus(ScheduledFeeStatus.CANCELLED);
        this.scheduledFeeRepository.save(fee);

        this.applicationEventPublisher.publishEvent(new ScheduledFeeCancelledEvent(fee.getId(), fee.getScheduledDate()));

        log.info("Scheduled fee: {} cancelled successfully", fee.getId());
    }

    @Transactional
    @Override
    public void waiveScheduledFee(Long id) {
        ScheduledFee fee = this.scheduledFeeRepository.findByIdForUpdate(id).orElseThrow(() -> {
            log.warn("Scheduled fee not found with id: {}", id);
            return new ApiResourceException("Scheduled fee not found", HttpStatus.NOT_FOUND);
        });

        if (fee.getStatus() != ScheduledFeeStatus.PENDING && fee.getStatus() != ScheduledFeeStatus.FAILED) {
            log.warn("Scheduled fee: {} cannot be waived", fee.getId());
            throw new ApiResourceException("Only pending or failed scheduled fees can be waived", HttpStatus.BAD_REQUEST);
        }

        fee.setStatus(ScheduledFeeStatus.WAIVED);
        this.scheduledFeeRepository.save(fee);

        this.applicationEventPublisher.publishEvent(new ScheduledFeeWaivedEvent(fee.getId(), fee.getScheduledDate()));

        log.info("Scheduled fee: {} waived successfully", fee.getId());
    }
}
