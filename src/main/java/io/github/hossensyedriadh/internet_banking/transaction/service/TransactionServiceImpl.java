package io.github.hossensyedriadh.internet_banking.transaction.service;

import io.github.hossensyedriadh.internet_banking.common.AccountModel;
import io.github.hossensyedriadh.internet_banking.common.CurrencyModel;
import io.github.hossensyedriadh.internet_banking.http.cbs.CoreBankingSolutionService;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.*;
import io.github.hossensyedriadh.internet_banking.transaction.model.TransactionModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
class TransactionServiceImpl implements TransactionService {
    private final CoreBankingSolutionService coreBankingSolutionService;
    private final TaskExecutor taskExecutor;

    @Autowired
    public TransactionServiceImpl(CoreBankingSolutionService coreBankingSolutionService, @Qualifier("iBankingTaskExecutor") TaskExecutor taskExecutor) {
        this.coreBankingSolutionService = coreBankingSolutionService;
        this.taskExecutor = taskExecutor;
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
}
