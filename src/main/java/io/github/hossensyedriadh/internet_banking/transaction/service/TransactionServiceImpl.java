package io.github.hossensyedriadh.internet_banking.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hossensyedriadh.internet_banking.beneficiary.model.BeneficiaryModel;
import io.github.hossensyedriadh.internet_banking.beneficiary.service.BeneficiaryService;
import io.github.hossensyedriadh.internet_banking.common.AccountModel;
import io.github.hossensyedriadh.internet_banking.common.CurrencyModel;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingException;
import io.github.hossensyedriadh.internet_banking.exception.CoreBankingTransactionNotFoundException;
import io.github.hossensyedriadh.internet_banking.exception.ResourceException;
import io.github.hossensyedriadh.internet_banking.http.cbs.CoreBankingSolutionService;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.*;
import io.github.hossensyedriadh.internet_banking.transaction.entity.IbankingTransaction;
import io.github.hossensyedriadh.internet_banking.transaction.model.FundTransferRequest;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionAcquisition;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionModel;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
class TransactionServiceImpl implements TransactionService {
    private final CoreBankingSolutionService coreBankingSolutionService;
    private final ChannelTransactionService channelTransactionService;
    private final BeneficiaryService beneficiaryService;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    @Autowired
    public TransactionServiceImpl(CoreBankingSolutionService coreBankingSolutionService, ChannelTransactionService channelTransactionService,
                                  BeneficiaryService beneficiaryService, @Qualifier("iBankingTaskExecutor") TaskExecutor taskExecutor, ObjectMapper objectMapper) {
        this.coreBankingSolutionService = coreBankingSolutionService;
        this.channelTransactionService = channelTransactionService;
        this.beneficiaryService = beneficiaryService;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public Page<TransactionModel> fetchTransactions(CbsTransactionsSearch search) {
        CbsTransactionPage transactionsPage = this.coreBankingSolutionService.fetchTransactions(search);

        if (transactionsPage.getContent().isEmpty()) {
            return Page.empty();
        }

        List<CbsTransactionResponse> transactions = transactionsPage.getContent();

        Set<Long> accountIds = transactions.stream().map(CbsTransactionResponse::getAccountId).collect(Collectors.toSet());
        Set<Long> currencyIds = transactions.stream().map(CbsTransactionResponse::getCurrencyId).collect(Collectors.toSet());

        Map<Long, CompletableFuture<CbsAccountResponse>> accountFutures = accountIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> CompletableFuture.supplyAsync(() -> this.coreBankingSolutionService.fetchAccountInfo(id), this.taskExecutor)));

        Map<Long, CompletableFuture<CbsCurrencyResponse>> currencyFutures = currencyIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> CompletableFuture.supplyAsync(() -> this.coreBankingSolutionService.fetchCurrencyInfo(id), this.taskExecutor)));

        CompletableFuture.allOf(Stream.concat(accountFutures.values().stream(), currencyFutures.values().stream()).toArray(CompletableFuture[]::new)).join();

        Map<Long, CbsAccountResponse> accountMap = accountFutures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().join()));

        Map<Long, CbsCurrencyResponse> currencyMap = currencyFutures.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().join()));

        List<TransactionModel> transactionModels = transactions.stream().map(cbsTransaction -> {
            CbsAccountResponse accountResponse = accountMap.get(cbsTransaction.getAccountId());
            CbsCurrencyResponse currencyResponse = currencyMap.get(cbsTransaction.getCurrencyId());

            return this.prepareTransactionModel(cbsTransaction, accountResponse, currencyResponse);
        }).toList();

        return new PageImpl<>(transactionModels, Pageable.ofSize(transactionsPage.getPage().size()), transactionsPage.getPage().totalElements());
    }

    @Override
    public TransactionModel fetchTransaction(Long id) {
        CbsTransactionResponse transactionResponse = this.coreBankingSolutionService.fetchTransaction(id);
        CbsAccountResponse accountResponse = this.coreBankingSolutionService.fetchAccountInfo(transactionResponse.getAccountId());
        CbsCurrencyResponse currencyResponse = this.coreBankingSolutionService.fetchCurrencyInfo(transactionResponse.getCurrencyId());

        return this.prepareTransactionModel(transactionResponse, accountResponse, currencyResponse);
    }

    @Override
    public TransactionModel transfer(UUID idempotencyKey, @Valid FundTransferRequest request) {
        TransactionAcquisition acquisition = this.channelTransactionService.createTransaction(idempotencyKey, request);
        IbankingTransaction channelTransaction = acquisition.transaction();

        if (!acquisition.created()) {
            return this.handleExistingTransaction(channelTransaction);
        }

        Long channelTransactionId = channelTransaction.getId();

        if (!this.channelTransactionService.claimForProcessing(channelTransactionId)) {
            log.error("Unable to acquire transaction for processing");
            throw new ResourceException("Unable to acquire transaction for processing", HttpStatus.CONFLICT);
        }

        BeneficiaryModel beneficiary = this.getBeneficiary(request.getBeneficiaryId());
        CbsAccountResponse destinationAccount = this.coreBankingSolutionService.fetchAccountInfo(beneficiary.getAccountNumber());

        CbsFundTransferRequest cbsRequest = new CbsFundTransferRequest();
        cbsRequest.setAccountId(request.getAccountId());
        cbsRequest.setDestinationAccountId(destinationAccount.getId());
        cbsRequest.setCurrencyId(request.getCurrencyId());
        cbsRequest.setAmount(request.getAmount());
        cbsRequest.setIdempotencyKey(channelTransaction.getIdempotencyKey().toString());
        cbsRequest.setDescription(request.getDescription());

        CbsTransactionResponse cbsResponse;

        try {
            cbsResponse = this.coreBankingSolutionService.fundTransfer(cbsRequest);
        } catch (CoreBankingCommunicationException e) {
            this.channelTransactionService.markTransactionUnknown(channelTransactionId);
            log.error("Unable to determine CBS outcome [Channel transaction ID: {}]", channelTransactionId, e);
            throw e;
        }

        this.channelTransactionService.recordCbsResult(channelTransactionId, cbsResponse);

        log.info("Fund transfer processed [Channel transaction ID: {}, CBS transaction ID: {}, Status: {}]", channelTransactionId, cbsResponse.getId(), cbsResponse.getStatus());

        CbsAccountResponse accountResponse = this.coreBankingSolutionService.fetchAccountInfo(cbsResponse.getAccountId());
        CbsCurrencyResponse currencyResponse = this.coreBankingSolutionService.fetchCurrencyInfo(cbsResponse.getCurrencyId());

        return this.prepareTransactionModel(cbsResponse, accountResponse, currencyResponse);
    }

    private TransactionModel prepareTransactionModel(CbsTransactionResponse transactionResponse, CbsAccountResponse accountResponse, CbsCurrencyResponse currencyResponse) {
        AccountModel account = new AccountModel();
        account.setAccountId(accountResponse.getId());
        account.setAccountNumber(accountResponse.getAccountNumber());
        account.setStatus(accountResponse.getStatus());
        account.setOpenedAt(accountResponse.getOpenedAt());

        CurrencyModel currency = new CurrencyModel();
        currency.setId(currencyResponse.getId());
        currency.setCode(currencyResponse.getCode());
        currency.setName(currencyResponse.getName());
        currency.setSymbol(currencyResponse.getSymbol());
        currency.setDecimalPlaces(currencyResponse.getDecimalPlaces());

        TransactionModel transaction = new TransactionModel();
        transaction.setId(transactionResponse.getId());
        transaction.setTransactionReference(transactionResponse.getTransactionReference());
        transaction.setAccount(account);
        transaction.setCurrency(currency);
        transaction.setAmount(transactionResponse.getAmount());
        transaction.setStatus(transactionResponse.getStatus());
        transaction.setTransactionType(transactionResponse.getTransactionType());
        transaction.setDescription(transactionResponse.getDescription());
        transaction.setCreatedAt(transactionResponse.getCreatedAt());
        transaction.setCompletedAt(transactionResponse.getCompletedAt());

        return transaction;
    }

    private TransactionModel handleExistingTransaction(IbankingTransaction transaction) {
        return switch (transaction.getStatus()) {
            case APPROVED, DECLINED, FAILED -> this.prepareStoredTransactionModel(transaction);
            case PROCESSING -> {
                log.warn("This transaction [ID: {}] is already being processed", transaction.getId());
                throw new ResourceException("This transaction is already being processed", HttpStatus.CONFLICT);
            }
            case UNKNOWN -> this.reconcileUnknownTransaction(transaction);
            case RECEIVED -> {
                log.warn("This transaction [ID: {}] has not started processing yet. Please retry shortly", transaction.getId());
                throw new ResourceException("This transaction has not started processing yet. Please retry shortly", HttpStatus.CONFLICT);
            }
        };
    }

    private TransactionModel prepareStoredTransactionModel(IbankingTransaction transaction) {
        CbsTransactionResponse cbsResponse;

        try {
            cbsResponse = this.objectMapper.readValue(transaction.getResponsePayload(), CbsTransactionResponse.class);
        } catch (IOException e) {
            log.error("Unable to deserialize stored CBS transaction response for channel transaction ID: {}", transaction.getId());
            throw new IllegalStateException("Unable to deserialize stored CBS transaction response", e);
        }

        CbsAccountResponse accountResponse = this.coreBankingSolutionService.fetchAccountInfo(cbsResponse.getAccountId());
        CbsCurrencyResponse currencyResponse = this.coreBankingSolutionService.fetchCurrencyInfo(cbsResponse.getCurrencyId());

        return this.prepareTransactionModel(cbsResponse, accountResponse, currencyResponse);
    }

    private TransactionModel reconcileUnknownTransaction(IbankingTransaction transaction) {
        Long transactionId = transaction.getId();

        if (!this.channelTransactionService.claimForReconciliation(transactionId)) {
            throw new ResourceException("Transaction reconciliation is already in progress", HttpStatus.CONFLICT);
        }

        CbsTransactionResponse cbsResponse;

        try {
            cbsResponse = this.coreBankingSolutionService.fetchTransaction(transaction.getIdempotencyKey(), transaction.getCbsAccountId());

        } catch (CoreBankingTransactionNotFoundException e) {
            log.warn("CBS transaction not found. Attempting recovery [Channel transaction ID: {}]", transactionId);

            try {
                FundTransferRequest originalRequest = this.objectMapper.readValue(transaction.getRequestPayload(), FundTransferRequest.class);

                BeneficiaryModel beneficiary = this.getBeneficiary(originalRequest.getBeneficiaryId());
                CbsAccountResponse destinationAccount = this.coreBankingSolutionService.fetchAccountInfo(beneficiary.getAccountNumber());

                CbsFundTransferRequest cbsRequest = new CbsFundTransferRequest();
                cbsRequest.setAccountId(transaction.getCbsAccountId());
                cbsRequest.setDestinationAccountId(destinationAccount.getId());
                cbsRequest.setCurrencyId(originalRequest.getCurrencyId());
                cbsRequest.setAmount(originalRequest.getAmount());
                cbsRequest.setDescription(originalRequest.getDescription());
                cbsRequest.setIdempotencyKey(transaction.getIdempotencyKey().toString());

                cbsResponse = this.coreBankingSolutionService.fundTransfer(cbsRequest);
            } catch (CoreBankingCommunicationException ex) {
                this.channelTransactionService.markTransactionUnknown(transactionId);
                log.error("Communication failure during transaction recovery [Channel transaction ID: {}]", transactionId, ex);
                throw ex;
            } catch (CoreBankingException ex) {
                this.channelTransactionService.markTransactionUnknown(transactionId);
                log.error("CBS error during transaction recovery [Channel transaction ID: {}]", transactionId, ex);
                throw ex;

            } catch (IOException ex) {
                this.channelTransactionService.markTransactionUnknown(transactionId);
                log.error("Unable to deserialize original transfer request [Channel transaction ID: {}]", transactionId, ex);
                throw new IllegalStateException("Unable to recover original transfer request", ex);
            }

        } catch (CoreBankingCommunicationException e) {
            this.channelTransactionService.markTransactionUnknown(transactionId);
            log.error("Communication failure during transaction reconciliation [Channel transaction ID: {}]", transactionId, e);
            throw e;

        } catch (CoreBankingException e) {
            this.channelTransactionService.markTransactionUnknown(transactionId);
            log.error("CBS error during transaction reconciliation [Channel transaction ID: {}]", transactionId, e);
            throw e;
        }

        this.channelTransactionService.recordCbsResult(transactionId, cbsResponse);

        CbsAccountResponse accountResponse = this.coreBankingSolutionService.fetchAccountInfo(cbsResponse.getAccountId());
        CbsCurrencyResponse currencyResponse = this.coreBankingSolutionService.fetchCurrencyInfo(cbsResponse.getCurrencyId());

        return this.prepareTransactionModel(cbsResponse, accountResponse, currencyResponse);
    }

    private BeneficiaryModel getBeneficiary(UUID id) {
        Optional<BeneficiaryModel> beneficiaryModel = this.beneficiaryService.findByIdForPrincipal(id);

        if (beneficiaryModel.isEmpty()) {
            log.warn("Beneficiary [ID: {}] not found", id);
            throw new ResourceException("Beneficiary not found", HttpStatus.NOT_FOUND);
        }

        return beneficiaryModel.get();
    }
}
