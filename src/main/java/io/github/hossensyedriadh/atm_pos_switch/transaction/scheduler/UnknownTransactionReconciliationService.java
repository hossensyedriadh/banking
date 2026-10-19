package io.github.hossensyedriadh.atm_pos_switch.transaction.scheduler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionType;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingTransactionResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingTransactionsSearch;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CoreBankingService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseReconstructor;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
public class UnknownTransactionReconciliationService {
    private static final int BATCH_SIZE = 100;

    private final SwitchTransactionService switchTransactionService;
    private final CoreBankingService coreBankingService;
    private final IsoResponseReconstructor isoResponseReconstructor;

    @Autowired
    public UnknownTransactionReconciliationService(SwitchTransactionService switchTransactionService, CoreBankingService coreBankingService, IsoResponseReconstructor isoResponseReconstructor) {
        this.switchTransactionService = switchTransactionService;
        this.coreBankingService = coreBankingService;
        this.isoResponseReconstructor = isoResponseReconstructor;
    }

    @Scheduled(fixedDelayString = "${switch.reconciliation.fixed-delay:30000}")
    public void reconcileUnknownTransactions() {
        this.reconcileUnknownWithdrawals();
        this.reconcileUnknownReversals();
    }

    private void reconcileUnknownWithdrawals() {
        List<SwitchTransactionSummary> transactions = this.switchTransactionService.findUnknownWithdrawals(BATCH_SIZE);

        for (SwitchTransactionSummary transaction : transactions) {
            try {
                this.reconcileWithdrawal(transaction);
            } catch (CoreBankingCommunicationException e) {
                log.warn("CBS lookup failed for unknown withdrawal {}. Will retry later.", transaction.id(), e);
            } catch (RuntimeException e) {
                log.error("Failed to reconcile unknown withdrawal {}. Will retry later.", transaction.id(), e);
            }
        }
    }

    private void reconcileUnknownReversals() {
        List<SwitchTransactionSummary> reversals = this.switchTransactionService.findUnknownReversals(BATCH_SIZE);

        for (SwitchTransactionSummary reversal : reversals) {
            try {
                this.reconcileUnknownReversal(reversal);
            } catch (CoreBankingCommunicationException e) {
                log.warn("CBS lookup failed for unknown reversal {}. Will retry later.", reversal.id(), e);
            } catch (RuntimeException e) {
                log.error("Failed to reconcile unknown reversal {}. Will retry later.", reversal.id(), e);
            }
        }
    }

    private void reconcileWithdrawal(SwitchTransactionSummary transaction) {
        String idempotencyKey = Long.toString(transaction.id());

        Optional<CoreBankingTransactionResponse> result = this.coreBankingService.findTransactionByIdempotencyKey(idempotencyKey);

        if (result.isEmpty()) {
            OffsetDateTime from = transaction.receivedAt().minusMinutes(30);
            OffsetDateTime to = transaction.receivedAt().plusMinutes(30);

            result = this.findInCbsListing(idempotencyKey, from, to);
        }

        if (result.isEmpty()) {
            log.debug("No CBS transaction found for switch withdrawal {}. Will retry later.", transaction.id());
            return;
        }

        CoreBankingTransactionResponse cbsTransaction = result.get();

        if (!this.hasMatchingIdempotencyKey(cbsTransaction, idempotencyKey)) {
            return;
        }

        if (cbsTransaction.getId() == null) {
            log.error("CBS returned no transaction ID for switch withdrawal {}.", transaction.id());
            return;
        }

        if (cbsTransaction.getTransactionType() != TransactionType.WITHDRAWAL) {
            log.error("Transaction type mismatch for switch transaction {}: expected WITHDRAWAL, got {}.", transaction.id(), cbsTransaction.getTransactionType());
            return;
        }

        if (cbsTransaction.getAmount() == null || transaction.transactionAmount() == null
                || cbsTransaction.getAmount().compareTo(transaction.transactionAmount()) != 0) {
            log.error("Amount mismatch for switch withdrawal {}: switch amount={}, CBS amount={}.", transaction.id(),
                    transaction.transactionAmount(), cbsTransaction.getAmount());
            return;
        }

        if (cbsTransaction.getCurrencyId() == null) {
            log.error("CBS transaction {} has no currency ID; leaving switch withdrawal {} UNKNOWN.", cbsTransaction.getId(), transaction.id());
            return;
        }

        switch (cbsTransaction.getStatus()) {
            case COMPLETED -> {
                Optional<Long> expectedCurrencyId = this.switchTransactionService.findCbsCurrencyId(transaction.id());

                if (expectedCurrencyId.isEmpty()) {
                    log.error("Original CBS currency ID is missing for switch withdrawal {}. Leaving UNKNOWN.", transaction.id());
                    return;
                }

                if (!Objects.equals(cbsTransaction.getCurrencyId(), expectedCurrencyId.get())) {
                    log.error(
                            "Currency mismatch for switch withdrawal {}: CBS currency ID {}, expected {}. Leaving UNKNOWN.", transaction.id(), cbsTransaction.getCurrencyId(), expectedCurrencyId.get());
                    return;
                }

                this.resolveCompletedWithdrawal(transaction, cbsTransaction);
            }

            case PENDING, FAILED, REVERSED ->
                    log.info("Switch withdrawal {} remains UNKNOWN because CBS status is {}.", transaction.id(), cbsTransaction.getStatus());
        }
    }

    private void resolveCompletedWithdrawal(SwitchTransactionSummary transaction, CoreBankingTransactionResponse cbsTransaction) {
        Optional<byte[]> requestPayload = this.switchTransactionService.findRequestPayload(transaction.id());

        if (requestPayload.isEmpty()) {
            log.error("Cannot reconcile switch withdrawal {}: original request payload is missing.", transaction.id());
            return;
        }

        final byte[] responsePayload;

        try {
            responsePayload = this.isoResponseReconstructor.reconstructApprovedResponse(requestPayload.get());
        } catch (RuntimeException e) {
            log.error("Cannot reconstruct ISO response for switch withdrawal {}.", transaction.id(), e);
            return;
        }

        int updated = this.switchTransactionService.resolveUnknownTransaction(transaction.id(), ResponseCodes.APPROVED, TransactionStatus.APPROVED, cbsTransaction.getId(), responsePayload);

        if (updated == 1) {
            log.info("Reconciled switch withdrawal {} as APPROVED using CBS transaction {}.", transaction.id(), cbsTransaction.getId());
        } else {
            log.debug("Switch withdrawal {} was no longer UNKNOWN; no changes made.", transaction.id());
        }
    }

    private void reconcileUnknownReversal(SwitchTransactionSummary reversal) {
        Optional<Long> originalId = this.switchTransactionService.findOriginalTransactionId(reversal.id());

        if (originalId.isEmpty()) {
            log.error("Cannot reconcile reversal {}: original transaction link is missing.", reversal.id());
            return;
        }

        Optional<SwitchTransactionSummary> originalResult = this.switchTransactionService.findTransactionById(originalId.get());

        if (originalResult.isEmpty()) {
            log.error("Cannot reconcile reversal {}: original switch transaction {} not found.", reversal.id(), originalId.get());
            return;
        }

        SwitchTransactionSummary original = originalResult.get();

        if (original.cbsTxnId() == null) {
            log.error("Cannot reconcile reversal {}: original CBS transaction ID is missing.", reversal.id());
            return;
        }

        String expectedIdempotencyKey = Long.toString(reversal.id());

        Optional<CoreBankingTransactionResponse> result = this.coreBankingService.findTransactionByIdempotencyKey(Long.toString(reversal.id()));

        if (result.isEmpty()) {
            OffsetDateTime from = reversal.receivedAt().minusMinutes(30);
            OffsetDateTime to = reversal.receivedAt().plusMinutes(30);

            result = this.findInCbsListing(expectedIdempotencyKey, from, to);
        }

        if (result.isEmpty()) {
            log.debug("No CBS reversal found for switch reversal {}. Will retry later.", reversal.id());
            return;
        }

        CoreBankingTransactionResponse cbsReversal = result.get();

        if (!this.hasMatchingIdempotencyKey(cbsReversal, expectedIdempotencyKey)) {
            return;
        }

        if (cbsReversal.getId() == null || cbsReversal.getTransactionType() != TransactionType.REVERSAL
                || !Objects.equals(cbsReversal.getReversalOfTransactionId(), original.cbsTxnId())) {
            log.error("CBS reversal validation failed for switch reversal {}. Leaving UNKNOWN.", reversal.id());
            return;
        }

        if (cbsReversal.getAmount() == null || reversal.transactionAmount() == null
                || cbsReversal.getAmount().compareTo(reversal.transactionAmount()) != 0) {
            log.error("Amount mismatch for switch reversal {}: switch amount={}, CBS amount={}. Leaving UNKNOWN.",
                    reversal.id(), reversal.transactionAmount(), cbsReversal.getAmount());
            return;
        }

        if (cbsReversal.getCurrencyId() == null) {
            log.error("CBS reversal {} has no currency ID. Leaving switch reversal {} UNKNOWN.", cbsReversal.getId(), reversal.id());
            return;
        }

        Optional<Long> expectedCurrencyId = this.switchTransactionService.findCbsCurrencyId(original.id());

        if (expectedCurrencyId.isEmpty() || !Objects.equals(cbsReversal.getCurrencyId(), expectedCurrencyId.get())) {
            log.error(
                    "Currency mismatch for switch reversal {}: CBS currency ID={}, expected={}. Leaving UNKNOWN.",
                    reversal.id(), cbsReversal.getCurrencyId(), expectedCurrencyId.orElse(null));
            return;
        }

        if (cbsReversal.getStatus() != io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.TransactionStatus.COMPLETED) {
            log.info("Switch reversal {} remains UNKNOWN because CBS status is {}.", reversal.id(), cbsReversal.getStatus());
            return;
        }

        Optional<byte[]> requestPayload = this.switchTransactionService.findRequestPayload(reversal.id());

        if (requestPayload.isEmpty()) {
            log.error("Cannot reconcile switch reversal {}: original 0400 request payload is missing.", reversal.id());
            return;
        }

        final byte[] responsePayload;

        try {
            responsePayload = this.isoResponseReconstructor.reconstructApprovedResponse(requestPayload.get());
        } catch (RuntimeException e) {
            log.error("Cannot reconstruct ISO response for switch reversal {}.", reversal.id(), e);
            return;
        }

        int updated = this.switchTransactionService.resolveUnknownReversalAndUpdateOriginal(reversal.id(), original.cbsTxnId(), ResponseCodes.APPROVED, cbsReversal.getId(), responsePayload);

        if (updated == 1) {
            log.info("Resolved UNKNOWN reversal {} as APPROVED using CBS reversal {}.", reversal.id(), cbsReversal.getId());
        } else {
            log.debug("Switch reversal {} was no longer UNKNOWN; no changes made.", reversal.id());
        }
    }

    private boolean hasMatchingIdempotencyKey(CoreBankingTransactionResponse transaction, String expectedIdempotencyKey) {
        String actualIdempotencyKey = transaction.getIdempotencyKey();

        if (actualIdempotencyKey == null || actualIdempotencyKey.isBlank()) {
            return true;
        }

        if (!expectedIdempotencyKey.equals(actualIdempotencyKey)) {
            log.error("CBS idempotency key mismatch. Expected: {}, actual: {}", expectedIdempotencyKey, actualIdempotencyKey);
            return false;
        }

        return true;
    }

    private Optional<CoreBankingTransactionResponse> findInCbsListing(String expectedIdempotencyKey, OffsetDateTime from, OffsetDateTime to) {
        CoreBankingTransactionsSearch search = new CoreBankingTransactionsSearch(from, to, null, null, null, 0, 100);

        return this.coreBankingService.findAllTransactions(search).stream().filter(transaction ->
                expectedIdempotencyKey.equals(transaction.getIdempotencyKey())).findFirst();
    }
}

