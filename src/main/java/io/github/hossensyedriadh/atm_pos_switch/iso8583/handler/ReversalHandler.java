package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingReversalResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CoreBankingService;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.parser.IsoMessageParser;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.OriginalDataElements;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            return this.isoResponseFactory.createResponse(request, "30");
        }

        Optional<SwitchTransactionSummary> original = this.switchTransactionService.findOriginalTransaction(terminalId, originalDataElements);

        if (original.isEmpty()) {
            return this.isoResponseFactory.createResponse(request, "25");
        }

        SwitchTransactionSummary originalTransaction = original.get();

        if (originalTransaction.status() != TransactionStatus.APPROVED
                || !"00".equals(originalTransaction.responseCode())) {
            return this.isoResponseFactory.createResponse(request, "12");
        }

        Optional<Long> reversalId = this.switchTransactionService.registerReversalRequest(request.getString(41), request.getString(11),
                request.getString(37), request.getString(3), request.getMTI(),
                request.getString(7), originalTransaction.id());

        if (reversalId.isEmpty()) {
            return this.isoResponseFactory.createResponse(request, "94");
        }

        long transactionId = reversalId.get();

        this.switchTransactionService.markProcessing(transactionId);

        try {
            CoreBankingReversalResult result = this.coreBankingService.reverseTransaction(originalTransaction, Long.toString(transactionId));

            String responseCode = result.getResponseCode();

            if (responseCode == null || !responseCode.matches("\\d{2}")) {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.UNKNOWN);
                return this.isoResponseFactory.createResponse(request, "96");
            }

            ISOMsg response = this.isoResponseFactory.createResponse(request, responseCode);

            byte[] responsePayload = this.isoMessageParser.pack(response);

            TransactionStatus status = result.isSuccessful() ? TransactionStatus.APPROVED : TransactionStatus.DECLINED;

            if (result.isSuccessful() && !"00".equals(responseCode)) {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.UNKNOWN);
                return isoResponseFactory.createResponse(request, "96");
            }

            if (!result.isSuccessful() && "00".equals(responseCode)) {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.UNKNOWN);
                return isoResponseFactory.createResponse(request, "96");
            }

            this.switchTransactionService.updateCbsTransactionIds(originalTransaction.id(), originalTransaction.cbsTxnId(), result.getCbsReversalTransactionId());
            this.switchTransactionService.updateCbsTransactionIds(transactionId, result.getCbsReversalTransactionId(), null);

            this.switchTransactionService.complete(transactionId, responseCode, status, responsePayload);

            return response;
        } catch (RuntimeException e) {
            try {
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.UNKNOWN);
            } catch (RuntimeException ex) {
                e.addSuppressed(ex);
            }

            log.error("CBS transaction reversal failed. reversalTransactionId={}, originalTransactionId={}",
                    transactionId, originalTransaction.id(), e);

            return this.isoResponseFactory.createResponse(request, "96");
        }
    }
}
