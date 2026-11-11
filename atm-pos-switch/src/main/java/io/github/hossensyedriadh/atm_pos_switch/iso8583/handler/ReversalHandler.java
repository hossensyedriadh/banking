package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CoreBankingService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.parser.IsoMessageParser;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.OriginalDataElements;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.ReversalRegistrationResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
public class ReversalHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final SwitchTransactionService switchTransactionService;
    private final CoreBankingService coreBankingService;
    private final IsoMessageParser isoMessageParser;

    @Autowired
    public ReversalHandler(IsoResponseFactory isoResponseFactory, SwitchTransactionService switchTransactionService,
                           CoreBankingService coreBankingService, IsoMessageParser isoMessageParser) {
        this.isoResponseFactory = isoResponseFactory;
        this.switchTransactionService = switchTransactionService;
        this.coreBankingService = coreBankingService;
        this.isoMessageParser = isoMessageParser;
    }

    public ISOMsg handle(ISOMsg request) throws ISOException {
        String terminalId = request.getString(41);

        OriginalDataElements originalDataElements;

        try {
            originalDataElements = OriginalDataElements.parse(request.getString(90));
        } catch (IllegalArgumentException e) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.FORMAT_ERROR);
        }

        Optional<SwitchTransactionSummary> original = this.switchTransactionService.findOriginalTransaction(terminalId, originalDataElements);

        if (original.isEmpty()) {
            return this.isoResponseFactory.createResponse(request, "25");
        }

        SwitchTransactionSummary originalTransaction = original.get();

        if (originalTransaction.status() != TransactionStatus.APPROVED || !ResponseCodes.APPROVED.equals(originalTransaction.responseCode())) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_TRANSACTION);
        }

        ReversalRegistrationResult registration = this.switchTransactionService.registerReversalRequest(terminalId, request.getString(11), request.getString(37), request.getString(3),
                request.getMTI(), request.getString(7), originalTransaction.id());

        long transactionId;

        switch (registration.outcome()) {
            case DUPLICATE_REQUEST -> {
                Optional<byte[]> storedResponse =
                        this.switchTransactionService.findResponsePayload(
                                registration.transactionId());

                if (storedResponse.isPresent()) {
                    return this.isoMessageParser.unpack(storedResponse.get());
                }

                return this.isoResponseFactory.createResponse(request, ResponseCodes.DUPLICATE_TRANSACTION);
            }

            case ORIGINAL_ALREADY_REVERSED, REQUEST_IDENTITY_CONFLICT -> {
                return this.isoResponseFactory.createResponse(request, ResponseCodes.DUPLICATE_TRANSACTION);
            }

            case REGISTERED -> transactionId = registration.transactionId();

            default ->
                    throw new IllegalStateException("Unexpected reversal registration outcome: " + registration.outcome());
        }

        try {
            int processingStarted = this.switchTransactionService.markProcessing(transactionId);

            if (processingStarted != 1) {
                log.warn("Could not start reversal transaction {}; status is no longer RECEIVED", transactionId);

                return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
            }

            if (originalTransaction.cbsTxnId() == null) {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.FAILED);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
            }

            CoreBankingReversalResult result = this.coreBankingService.reverseTransaction(originalTransaction, Long.toString(transactionId));

            String responseCode = result.responseCode();

            if (responseCode == null || !responseCode.matches("\\d{2}")
                    || result.successful() != ResponseCodes.APPROVED.equals(responseCode)) {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.UNKNOWN);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
            }

            if (result.successful() && !Objects.equals(result.cbsReversalTransactionId(), originalTransaction.cbsTxnId())) {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.UNKNOWN);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
            }

            ISOMsg response = this.isoResponseFactory.createResponse(request, responseCode);

            byte[] responsePayload = this.isoMessageParser.pack(response);

            TransactionStatus status = result.successful() ? TransactionStatus.APPROVED : TransactionStatus.DECLINED;

            int completed = this.switchTransactionService.completeReversalAtomically(transactionId, originalTransaction.id(), originalTransaction.cbsTxnId(), responseCode, status, result.cbsReversalTransactionId(), responsePayload);

            if (completed == 1) {
                return response;
            }

            log.warn("Could not complete reversal transaction {} because its status changed", transactionId);

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        } catch (CoreBankingCommunicationException e) {
            log.error("CBS reversal outcome is uncertain. reversalTransactionId={}, originalTransactionId={}", transactionId, originalTransaction.id(), e);

            this.markFailedSafely(transactionId, TransactionStatus.UNKNOWN, e);

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        } catch (RuntimeException e) {
            log.error("Unexpected reversal processing error. reversalTransactionId={}, originalTransactionId={}", transactionId, originalTransaction.id(), e);

            this.markFailedSafely(transactionId, TransactionStatus.UNKNOWN, e);

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }
    }

    private void markFailedSafely(long transactionId, TransactionStatus status, RuntimeException originalException) {
        try {
            this.switchTransactionService.markFailed(transactionId, status);
        } catch (RuntimeException statusException) {
            originalException.addSuppressed(statusException);
        }
    }
}
