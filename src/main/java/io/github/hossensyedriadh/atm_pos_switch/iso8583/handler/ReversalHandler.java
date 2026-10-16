package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

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

    @Autowired
    public ReversalHandler(IsoResponseFactory isoResponseFactory, SwitchTransactionService switchTransactionService) {
        this.isoResponseFactory = isoResponseFactory;
        this.switchTransactionService = switchTransactionService;
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

        //todo: call CBS for reversal execution

        return this.isoResponseFactory.createResponse(request, "96");
    }
}
