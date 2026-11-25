package io.github.hossensyedriadh.internet_banking.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hossensyedriadh.internet_banking.account.enumerator.AccountStatus;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryModel;
import io.github.hossensyedriadh.internet_banking.beneficiary.service.BeneficiaryService;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingException;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingTransactionNotFoundException;
import io.github.hossensyedriadh.internet_banking.exception.ResourceException;
import io.github.hossensyedriadh.internet_banking.http.cbs.CoreBankingSolutionService;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsAccountResponse;
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
    private final BeneficiaryService beneficiaryService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TransactionReconciliationServiceImpl(ChannelTransactionService channelTransactionService, CoreBankingSolutionService coreBankingSolutionService,
                                                BeneficiaryService beneficiaryService, ObjectMapper objectMapper) {
        this.channelTransactionService = channelTransactionService;
        this.coreBankingSolutionService = coreBankingSolutionService;
        this.beneficiaryService = beneficiaryService;
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
                try {
                    CbsFundTransferRequest fundTransferRequest = this.buildCbsFundTransferRequest(transaction);

                    cbsTransactionResponse = this.coreBankingSolutionService.fundTransfer(fundTransferRequest);
                } catch (CoreBankingException cbe) {
                    this.channelTransactionService.markTransactionFailed(channelTransactionId);
                    log.warn("CBS definitively rejected fund transfer during reconciliation [Channel Transaction ID: {}]", channelTransactionId, e);
                    return;
                }
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

        BeneficiaryModel beneficiary = this.beneficiaryService.findById(originalRequest.getBeneficiaryId()).orElseThrow(() -> {
            log.warn("Beneficiary not found with ID: {} for fund transfer transaction with ID: {}", originalRequest.getBeneficiaryId(), transaction.getId());
            return new ResourceException("Beneficiary not found for fund transfer transaction");
        });

        CbsAccountResponse destinationAccount = this.coreBankingSolutionService.fetchAccountInfo(beneficiary.getAccountNumber());

        if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Fund transfer transaction [ID: {}] destination account [ID: {}] is not in active state", transaction.getId(), destinationAccount.getId());
            throw new ResourceException("Fund transfer destination account is not active");
        }

        CbsFundTransferRequest cbsRequest = new CbsFundTransferRequest();
        cbsRequest.setAccountId(originalRequest.getAccountId());
        cbsRequest.setDestinationAccountId(destinationAccount.getId());
        cbsRequest.setCurrencyId(originalRequest.getCurrencyId());
        cbsRequest.setAmount(originalRequest.getAmount());
        cbsRequest.setDescription(originalRequest.getDescription());
        cbsRequest.setIdempotencyKey(transaction.getIdempotencyKey().toString());

        return cbsRequest;
    }
}
