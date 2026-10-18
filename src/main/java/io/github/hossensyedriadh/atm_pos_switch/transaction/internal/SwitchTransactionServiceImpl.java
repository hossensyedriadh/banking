package io.github.hossensyedriadh.atm_pos_switch.transaction.internal;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionRegistrationOutcome;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.*;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
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
    public int markProcessing(long transactionId) {
        int updated = this.switchTransactionRepository.markAsProcessing(transactionId);

        if (updated != 1) {
            log.warn("Fail to mark transaction as PROCESSING: {}", transactionId);
            throw new RuntimeException("Could not mark transaction as PROCESSING: " + transactionId);
        }

        log.info("Marked transaction {} as {}", transactionId, TransactionStatus.PROCESSING.toString());

        return updated;
    }

    @Override
    public TransactionCompletionResult complete(long transactionId, String responseCode, TransactionStatus status, byte[] responsePayload) {
        int updated = this.switchTransactionRepository.completeTransaction(transactionId, responseCode, status, responsePayload);

        Optional<TransactionCompletionResult> result = this.switchTransactionRepository.findCompletionResult(transactionId, updated == 1);

        if (result.isEmpty()) {
            log.error("Transaction {} not found during completion", transactionId);
            throw new RuntimeException("Transaction not found during completion: " + transactionId);
        }

        TransactionCompletionResult completionResult = result.get();

        if (completionResult.completed()) {
            log.info("Completed transaction {} as {}", transactionId, status);
        } else {
            log.warn("Could not complete transaction {} because its status is {}. Returning the stored response if available.", transactionId, completionResult.currentStatus());
        }

        return completionResult;
    }

    @Override
    public void markFailed(long transactionId, TransactionStatus status) {
        int updated = this.switchTransactionRepository.markAsFailed(transactionId, status);

        if (updated != 1) {
            log.warn("Failed to mark transaction as {}: {}", status.toString(), transactionId);
            throw new RuntimeException("Could not mark transaction as " + status + ": " + transactionId);
        }

        log.info("Marked transaction {} as {}", transactionId, status.toString());
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
    public SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference, String processingCode, String requestMti, String transmissionDateTime,
                                                               BigDecimal transactionAmount, String currencyCode, byte[] requestPayload) {
        try {
            long transactionId = this.switchTransactionRepository.insertReceived(terminalId, stan, retrievalReference, processingCode, requestMti,
                    transmissionDateTime, transactionAmount, currencyCode, requestPayload);

            log.info("Registered transaction ID={}", transactionId);

            return new SwitchTransactionRegistrationResult(
                    transactionId, TransactionRegistrationOutcome.REGISTERED);
        } catch (DuplicateKeyException e) {
            if (!violatesDuplicateTransactionIndex(e)) {
                throw e;
            }

            DuplicateTransactionCandidate candidate = this.switchTransactionRepository.findDuplicateTransaction(
                            terminalId, stan, transmissionDateTime)
                    .orElseThrow(() -> {
                        log.warn("Duplicate transaction index was violated, but the original transaction could not be found");
                        return new IllegalStateException("Duplicate transaction index was violated", e);
                    });

            boolean sameRequest = Objects.equals(candidate.retrievalReference(), retrievalReference)
                    && Objects.equals(candidate.processingCode(), processingCode)
                    && Objects.equals(candidate.requestMti(), requestMti)
                    && candidate.transactionAmount() != null && transactionAmount != null
                    && candidate.transactionAmount().compareTo(transactionAmount) == 0
                    && Objects.equals(candidate.currencyCode() == null ? null : candidate.currencyCode().trim(),
                    currencyCode == null ? null : currencyCode.trim());

            if (!sameRequest) {
                log.warn("Transaction identity conflict: terminalId={}, STAN={}, transmissionDateTime={}", terminalId, stan, transmissionDateTime);
                return new SwitchTransactionRegistrationResult(candidate.id(), TransactionRegistrationOutcome.REQUEST_IDENTITY_CONFLICT);
            }

            log.info("Duplicate request matches existing transaction ID={}", candidate.id());

            return new SwitchTransactionRegistrationResult(candidate.id(), TransactionRegistrationOutcome.DUPLICATE_REQUEST);
        }
    }

    @Override
    public ReversalRegistrationResult registerReversalRequest(String terminalId, String stan, String retrievalReference, String processingCode,
                                                              String requestMti, String transmissionDateTime, long originalTransactionId) {
        return this.switchTransactionRepository.insertReversalReceived(terminalId, stan, retrievalReference, processingCode,
                requestMti, transmissionDateTime, originalTransactionId);
    }

    @Override
    public void updateCbsAccountAndCurrencyIds(long transactionId, Long accountId, Long currencyId) {
        int updated = this.switchTransactionRepository.updateCbsAccountAndCurrencyIds(transactionId, accountId, currencyId);

        if (updated != 1) {
            log.error("Could not persist CBS account and currency IDs for switch transaction {}", transactionId);
            throw new RuntimeException("Could not persist CBS account and currency IDs for switch transaction " + transactionId);
        }

        log.info("Updated CBS account and currency IDs for transaction ID: {}", transactionId);
    }

    @Override
    public void updateCbsTransactionIds(long id, Long cbsTxnId, @Nullable Long cbsReversalTxnId) {
        int updated = this.switchTransactionRepository.updateCbsTransactionIds(id, cbsTxnId, cbsReversalTxnId);

        if (updated != 1) {
            log.error("Could not persist CBS transaction IDs for switch transaction {}", id);
            throw new RuntimeException("Could not persist CBS transaction IDs for switch transaction " + id);
        }

        log.info("Updated CBS transaction IDs for transaction ID: {}", id);
    }

    @Override
    public Optional<Long> findCbsCurrencyId(long transactionId) {
        return this.switchTransactionRepository.findCbsCurrencyId(transactionId);
    }

    @Override
    public List<SwitchTransactionSummary> findUnknownWithdrawals(int batchSize) {
        return this.switchTransactionRepository.findUnknownWithdrawals(batchSize);
    }

    @Override
    public List<SwitchTransactionSummary> findUnknownReversals(int batchSize) {
        return this.switchTransactionRepository.findUnknownReversals(batchSize);
    }

    @Override
    public int resolveUnknownTransaction(long id, String responseCode, TransactionStatus resolvedStatus, Long cbsTransactionId, byte[] responsePayload) {
        return this.switchTransactionRepository.resolveUnknownTransaction(id, responseCode, resolvedStatus, cbsTransactionId, responsePayload);
    }

    @Override
    public int resolveUnknownReversalAndUpdateOriginal(long reversalTransactionId, long originalCbsTransactionId, String responseCode, Long cbsReversalTransactionId, byte[] responsePayload) {
        return this.switchTransactionRepository.resolveUnknownReversalAndUpdateOriginal(reversalTransactionId, originalCbsTransactionId, responseCode, cbsReversalTransactionId, responsePayload);
    }

    @Override
    public int completeReversalAtomically(long reversalTransactionId, long originalTransactionId, Long originalCbsTransactionId, String responseCode, TransactionStatus resolvedStatus, Long cbsReversalTransactionId, byte[] responsePayload) {
        return this.switchTransactionRepository.completeReversalAtomically(reversalTransactionId, originalTransactionId, originalCbsTransactionId, responseCode, resolvedStatus, cbsReversalTransactionId, responsePayload);
    }

    @Override
    public Optional<byte[]> findResponsePayload(long transactionId) {
        return this.switchTransactionRepository.findResponsePayloadById(transactionId);
    }

    @Override
    public Optional<byte[]> findRequestPayload(long transactionId) {
        return this.switchTransactionRepository.findRequestPayload(transactionId);
    }

    @Override
    public Optional<TransactionStatus> findTransactionStatus(long transactionId) {
        return this.switchTransactionRepository.findStatusById(transactionId);
    }

    @Override
    public Optional<SwitchTransactionSummary> findTransactionById(long transactionId) {
        return this.switchTransactionRepository.findTransactionById(transactionId);
    }

    @Override
    public Optional<Long> findOriginalTransactionId(long reversalTransactionId) {
        return this.switchTransactionRepository.findOriginalTransactionId(reversalTransactionId);
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
