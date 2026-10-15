package io.github.hossensyedriadh.atm_pos_switch.transaction.internal;

import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionRegistrationResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
class SwitchTransactionServiceImpl implements SwitchTransactionService {
    private final SwitchTransactionRepository switchTransactionRepository;

    @Autowired
    public SwitchTransactionServiceImpl(SwitchTransactionRepository switchTransactionRepository) {
        this.switchTransactionRepository = switchTransactionRepository;
    }

    @Override
    public long recordReceived(String terminalId, String stan, String retrievalReference,
                               String processingCode, String requestMti, String transmissionDateTime) {
        return this.switchTransactionRepository.insertReceived(terminalId, stan, retrievalReference,
                processingCode, requestMti, transmissionDateTime);
    }

    @Override
    public void markProcessing(long transactionId) {
        int updated = this.switchTransactionRepository.markAsProcessing(transactionId);

        if (updated != 1) {
            log.warn("Fail to mark transaction as PROCESSING: {}", transactionId);
            throw new RuntimeException("Could not mark transaction as PROCESSING: " + transactionId);
        }
    }

    @Override
    public void complete(long transactionId, String responseCode, TransactionStatus status) {
        int updated = this.switchTransactionRepository.markAsComplete(transactionId, responseCode, status);

        if (updated != 1) {
            log.warn("Fail to mark transaction as COMPLETE: {}", transactionId);
            throw new RuntimeException("Could not complete transaction: " + transactionId);
        }
    }

    @Override
    public SwitchTransactionRegistrationResult registerRequest(String terminalId, String stan, String retrievalReference,
                                                               String processingCode, String requestMti,
                                                               String transmissionDateTime) {
        try {
            long transactionId = this.switchTransactionRepository.insertReceived(terminalId, stan, retrievalReference, processingCode,
                    requestMti, transmissionDateTime);

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

    private boolean violatesDuplicateTransactionIndex(Throwable exception) {
        for (Throwable cause = exception;
             cause != null;
             cause = cause.getCause()) {

            if (cause instanceof org.postgresql.util.PSQLException pgException) {
                var serverError = pgException.getServerErrorMessage();

                return serverError != null
                        && "uq_switch_transactions_terminal_stan_transmission"
                        .equals(serverError.getConstraint());
            }
        }

        return false;
    }
}
