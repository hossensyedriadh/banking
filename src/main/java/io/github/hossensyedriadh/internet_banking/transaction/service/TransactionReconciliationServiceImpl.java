package io.github.hossensyedriadh.internet_banking.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingTransactionNotFoundException;
import io.github.hossensyedriadh.internet_banking.http.cbs.CoreBankingSolutionService;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsFundTransferRequest;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsTransactionResponse;
import io.github.hossensyedriadh.internet_banking.transaction.entity.IbankingTransaction;
import io.github.hossensyedriadh.internet_banking.transaction.model.FundTransferRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Log4j2
@Service
public class TransactionReconciliationServiceImpl implements TransactionReconciliationService {
    private final ChannelTransactionService channelTransactionService;
    private final CoreBankingSolutionService coreBankingSolutionService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TransactionReconciliationServiceImpl(ChannelTransactionService channelTransactionService, CoreBankingSolutionService coreBankingSolutionService,
                                                ObjectMapper objectMapper) {
        this.channelTransactionService = channelTransactionService;
        this.coreBankingSolutionService = coreBankingSolutionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void reconcile(Long channelTransactionId) {
        if (!this.channelTransactionService.claimForReconciliation(channelTransactionId)) {
            log.warn("Skipping reconciliation of channel transaction [ID: {}], transaction is no longer eligible", channelTransactionId);
            return;
        }

        IbankingTransaction transaction = this.channelTransactionService.findById(channelTransactionId).orElseThrow(() -> {
            log.warn("Channel transaction not found with ID: {}", channelTransactionId);
            return new IllegalStateException("Channel transaction not found");
        });

        try {
            CbsTransactionResponse cbsTransactionResponse;

            try {
                cbsTransactionResponse = this.coreBankingSolutionService.fetchTransaction(transaction.getIdempotencyKey(), transaction.getCbsAccountId());

                log.info("Found existing CBS transaction during reconciliation [Channel Transaction ID: {}, CBS Transaction ID: {}]", channelTransactionId, cbsTransactionResponse.getId());
            } catch (CoreBankingTransactionNotFoundException e) {
                log.warn("CBS transaction not found; resubmitting original transaction request [Channel transaction ID: {}, Idempotency Key: {}]", channelTransactionId,
                        transaction.getIdempotencyKey().toString());

                CbsFundTransferRequest fundTransferRequest = this.buildCbsFundTransferRequest(transaction);
                cbsTransactionResponse = this.coreBankingSolutionService.fundTransfer(fundTransferRequest);
            }

            this.channelTransactionService.recordCbsResult(channelTransactionId, cbsTransactionResponse);

            log.info("Reconciliation completed [Channel transaction ID: {}, CBS Transaction ID: {}, Final Status: {}]", channelTransactionId, cbsTransactionResponse.getId(),
                    cbsTransactionResponse.getStatus());
        } catch (CoreBankingCommunicationException e) {
            this.channelTransactionService.markTransactionUnknown(channelTransactionId);

            log.error("Unable to determine CBS fund transfer result during reconciliation [Channel Transaction ID: {}]", channelTransactionId, e);

            throw e;
        }
    }

    private CbsFundTransferRequest buildCbsFundTransferRequest(IbankingTransaction transaction) {
        FundTransferRequest originalRequest;

        try {
            originalRequest = this.objectMapper.readValue(transaction.getRequestPayload(), FundTransferRequest.class);
        } catch (IOException e) {
            log.error("Unable to deserialize original fund transfer request [Channel transaction ID: {}", transaction.getId());
            throw new IllegalStateException("Unable to deserialize original fund transfer request", e);
        }

        CbsFundTransferRequest cbsRequest = new CbsFundTransferRequest();
        cbsRequest.setAccountId(originalRequest.getAccountId());
        cbsRequest.setDestinationAccountId(originalRequest.getDestinationAccountId());
        cbsRequest.setCurrencyId(originalRequest.getCurrencyId());
        cbsRequest.setAmount(originalRequest.getAmount());
        cbsRequest.setDescription(originalRequest.getDescription());
        cbsRequest.setIdempotencyKey(transaction.getIdempotencyKey().toString());

        return cbsRequest;
    }
}
