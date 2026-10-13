package io.github.hossensyedriadh.cbs.account.internal.service;

import io.github.hossensyedriadh.cbs.account.api.enumerator.FeeRuleStatus;
import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import io.github.hossensyedriadh.cbs.account.api.model.FeeRuleView;
import io.github.hossensyedriadh.cbs.account.api.service.FeeRuleService;
import io.github.hossensyedriadh.cbs.account.internal.domain.ScheduledFee;
import io.github.hossensyedriadh.cbs.account.internal.repository.ScheduledFeeRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.transaction.api.command.ChargeFeeCommand;
import io.github.hossensyedriadh.cbs.transaction.api.model.TransactionView;
import io.github.hossensyedriadh.cbs.transaction.api.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Log4j2
@Service
public class ScheduledFeeProcessor {
    private final ScheduledFeeRepository scheduledFeeRepository;
    private final ScheduledFeeResultService scheduledFeeResultService;
    private final FeeRuleService feeRuleService;
    private final TransactionService transactionService;

    @Autowired
    public ScheduledFeeProcessor(ScheduledFeeRepository scheduledFeeRepository, ScheduledFeeResultService scheduledFeeResultService,
                                 FeeRuleService feeRuleService, TransactionService transactionService) {
        this.scheduledFeeRepository = scheduledFeeRepository;
        this.scheduledFeeResultService = scheduledFeeResultService;
        this.feeRuleService = feeRuleService;
        this.transactionService = transactionService;
    }

    public void processFee(Long id) {
        ScheduledFee fee = this.scheduledFeeRepository.findById(id).orElseThrow(() -> {
            log.warn("Scheduled fee with ID {} not found", id);
            return new ApiResourceException("Scheduled fee not found", HttpStatus.NOT_FOUND);
        });

        if (fee.getStatus() != ScheduledFeeStatus.PROCESSING) {
            return;
        }

        try {
            FeeRuleView feeRule = this.feeRuleService.getFeeRule(fee.getFeeRuleId()).orElseThrow(() -> {
                log.warn("Fee rule not found by ID {}", fee.getFeeRuleId());
                return new ApiResourceException("Fee rule not found", HttpStatus.NOT_FOUND);
            });

            if (feeRule.getStatus() != FeeRuleStatus.ACTIVE) {
                log.warn("Fee rule is inactive: {}", feeRule.getFeeCode());
                throw new ApiResourceException("Fee rule is inactive", HttpStatus.BAD_REQUEST);
            }

            ChargeFeeCommand command = new ChargeFeeCommand(fee.getAccountId(), feeRule.getCurrencyId(),
                    fee.getAmount(), feeRule.getFeeCode(),
                    "SCHEDULED_FEE:" + fee.getId(),
                    "Scheduled fee: " + feeRule.getFeeName()
            );

            TransactionView transaction = this.transactionService.charge(command);

            this.scheduledFeeResultService.markCharged(fee.getId(), transaction.getId(), OffsetDateTime.now());

            log.info("Scheduled fee {} processed successfully", fee.getId());
        } catch (Exception exception) {
            log.error("Scheduled fee {} processing failed", id, exception);

            try {
                this.scheduledFeeResultService.markFailed(id, exception.getMessage());
            } catch (Exception persistenceException) {
                log.error("Could not persist failure status for scheduled fee {}. It may remain in PROCESSING state and require recovery.",
                        id, persistenceException);
            }
        }
    }
}
