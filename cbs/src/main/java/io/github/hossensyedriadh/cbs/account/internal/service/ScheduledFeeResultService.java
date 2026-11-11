package io.github.hossensyedriadh.cbs.account.internal.service;

import io.github.hossensyedriadh.cbs.account.api.enumerator.ScheduledFeeStatus;
import io.github.hossensyedriadh.cbs.account.internal.domain.ScheduledFee;
import io.github.hossensyedriadh.cbs.account.internal.repository.ScheduledFeeRepository;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Log4j2
@Service
public class ScheduledFeeResultService {
    private final ScheduledFeeRepository scheduledFeeRepository;

    @Autowired
    public ScheduledFeeResultService(ScheduledFeeRepository scheduledFeeRepository) {
        this.scheduledFeeRepository = scheduledFeeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCharged(Long id, Long transactionId, OffsetDateTime processedAt) {
        ScheduledFee fee = this.scheduledFeeRepository.findById(id).orElseThrow(() -> {
            log.warn("Scheduled fee with ID {} not found", id);
            return new ApiResourceException("Scheduled fee not found", HttpStatus.NOT_FOUND);
        });

        if (fee.getStatus() != ScheduledFeeStatus.PROCESSING) {
            log.warn("Scheduled fee with ID {} is not in processing state", id);
            throw new ApiResourceException("Scheduled fee is not in processing state", HttpStatus.BAD_REQUEST);
        }

        fee.setStatus(ScheduledFeeStatus.CHARGED);
        fee.setTransactionId(transactionId);
        fee.setProcessedAt(processedAt);
        fee.setProcessingStartedAt(null);
        fee.setFailureReason(null);

        this.scheduledFeeRepository.save(fee);

        log.info("Scheduled fee with ID {} marked as charged", id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long id, String reason) {
        ScheduledFee fee = this.scheduledFeeRepository.findById(id).orElseThrow(() -> {
            log.warn("Scheduled fee with ID {} not found", id);
            return new ApiResourceException("Scheduled fee not found", HttpStatus.NOT_FOUND);
        });

        if (fee.getStatus() != ScheduledFeeStatus.PROCESSING) {
            return;
        }

        fee.setStatus(ScheduledFeeStatus.FAILED);
        fee.setFailureReason(StringUtils.truncate(reason, 500));
        fee.setProcessingStartedAt(null);

        this.scheduledFeeRepository.save(fee);

        log.info("Scheduled fee with ID {} marked as failed", id);
    }
}
