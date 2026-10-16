package io.github.hossensyedriadh.atm_pos_switch.transaction.internal;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.OriginalDataElements;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionRegistrationResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Log4j2
@Service
class SwitchTransactionServiceImpl implements SwitchTransactionService {
    private final SwitchTransactionRepository switchTransactionRepository;

    @Autowired
    public SwitchTransactionServiceImpl(SwitchTransactionRepository switchTransactionRepository) {
        this.switchTransactionRepository = switchTransactionRepository;
    }

    @Override
    public void markProcessing(long transactionId) {
        int updated = this.switchTransactionRepository.markAsProcessing(transactionId);

        if (updated != 1) {
            log.warn("Fail to mark transaction as PROCESSING: {}", transactionId);
            throw new RuntimeException("Could not mark transaction as PROCESSING: " + transactionId);
        }

        log.info("Marked transaction as {}", TransactionStatus.PROCESSING.toString());
    }

    @Override
    public void complete(long transactionId, String responseCode, TransactionStatus status, byte[] responsePayload) {
        int updated = this.switchTransactionRepository.markAsComplete(transactionId, responseCode, status, responsePayload);

        if (updated != 1) {
            log.warn("Fail to mark transaction as COMPLETE: {}", transactionId);
            throw new RuntimeException("Could not complete transaction: " + transactionId);
        }

        log.info("Marked transaction as {}", status.toString());
    }

    @Override
    public void markFailed(long transactionId, TransactionStatus status) {
        int updated = this.switchTransactionRepository.markAsFailed(transactionId, status);

        if (updated != 1) {
            log.warn("Failed to mark transaction as {}: {}", status.toString(), transactionId);
            throw new RuntimeException("Could not mark transaction as " + status + ": " + transactionId);
        }

        log.info("Marked transaction as {}", status.toString());
    }

    @Override
    public int recoverStuckTransactions(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            log.warn("Timeout must be positive");
            throw new IllegalArgumentException("Timeout must be positive");
        }

        int updated = this.switchTransactionRepository.markStuckProcessingAsUnknown(timeout);

        if (updated > 0) {
            log.info("Marked {} transactions as {}", updated, TransactionStatus.UNKNOWN);
        }

        return updated;
    }

    @Override
    public SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference,
                                                               String processingCode, String requestMti,
                                                               String transmissionDateTime) {
        try {
            long transactionId = this.switchTransactionRepository.insertReceived(terminalId, stan, retrievalReference, processingCode,
                    requestMti, transmissionDateTime);

            log.info("Registered transaction request ID={}", transactionId);

            return new SwitchTransactionRegistrationResult(transactionId, false);
        } catch (DuplicateKeyException e) {
            if (!violatesDuplicateTransactionIndex(e)) {
                throw e;
            }

            return this.switchTransactionRepository.findIdByDuplicateKey(terminalId, stan, transmissionDateTime)
                    .map(id -> new SwitchTransactionRegistrationResult(id, true))
                    .orElseThrow(() -> {
                        log.warn("Duplicate transaction index was violated, but the original transaction could not be found");
                        return new RuntimeException("Duplicate transaction index was violated", e);
                    });
        }
    }

    @Override
    public Optional<Long> registerReversalRequest(String terminalId, String stan, String retrievalReference, String processingCode,
                                                  String requestMti, String transmissionDateTime, long originalTransactionId) {
        return this.switchTransactionRepository.insertReversalReceived(terminalId, stan, retrievalReference, processingCode,
                requestMti, transmissionDateTime, originalTransactionId);
    }

    @Override
    public Optional<byte[]> findResponsePayload(long transactionId) {
        return this.switchTransactionRepository.findResponsePayloadById(transactionId);
    }

    @Override
    public Optional<TransactionStatus> findTransactionStatus(long transactionId) {
        return this.switchTransactionRepository.findStatusById(transactionId);
    }

    @Override
    public Optional<SwitchTransactionSummary> findTransaction(String terminalId, String stan, String transmissionDateTime) {
        return this.switchTransactionRepository.findByTerminalStanAndTransmissionDateTime(terminalId, stan, transmissionDateTime);
    }

    @Override
    public Optional<SwitchTransactionSummary> findOriginalTransaction(String terminalId, OriginalDataElements originalData) {
        return this.switchTransactionRepository.findOriginalTransaction(terminalId, originalData);
    }

    @Override
    public boolean hasReversal(long originalTransactionId) {
        return this.switchTransactionRepository.hasReversal(originalTransactionId);
    }

    private boolean violatesDuplicateTransactionIndex(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {

            if (cause instanceof org.postgresql.util.PSQLException pgException) {
                var serverError = pgException.getServerErrorMessage();
                return serverError != null && "uq_switch_transactions_terminal_stan_transmission".equals(serverError.getConstraint());
            }
        }

        return false;
    }
}
