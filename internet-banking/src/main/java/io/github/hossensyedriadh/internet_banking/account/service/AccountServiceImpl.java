package io.github.hossensyedriadh.internet_banking.account.service;

import io.github.hossensyedriadh.internet_banking.account.model.*;
import io.github.hossensyedriadh.internet_banking.http.cbs.CoreBankingSolutionService;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.*;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class AccountServiceImpl implements AccountService {
    private final CoreBankingSolutionService coreBankingSolutionService;

    @Autowired
    public AccountServiceImpl(CoreBankingSolutionService coreBankingSolutionService) {
        this.coreBankingSolutionService = coreBankingSolutionService;
    }

    @Override
    public List<AccountOverviewModel> overview() {
        List<CbsAccountResponse> bankAccounts = this.coreBankingSolutionService.fetchBankAccounts();

        return bankAccounts.stream().map(this::prepareAccountOverview).toList();
    }

    @Override
    public AccountOverviewModel viewAccount(Long id) {
        CbsAccountResponse accountResponse = this.coreBankingSolutionService.fetchAccountInfo(id);
        return this.prepareAccountOverview(accountResponse);
    }

    @NonNull
    private AccountOverviewModel prepareAccountOverview(CbsAccountResponse account) {
        CbsAccountBalanceResponse balanceResponse = this.coreBankingSolutionService.fetchAccountBalance(account.getId());
        CbsCurrencyResponse currencyResponse = this.coreBankingSolutionService.fetchCurrencyInfo(account.getCurrencyId());
        CbsAccountProductResponse accountProductResponse = this.coreBankingSolutionService.fetchAccountProductInfo(account.getProductId());
        CbsBranchResponse branchResponse = this.coreBankingSolutionService.fetchBranchInfo(account.getBranchId());

        AccountBalanceModel balanceModel = new AccountBalanceModel();
        balanceModel.setAvailableBalance(balanceResponse.getAvailableBalance());
        balanceModel.setLedgerBalance(balanceResponse.getLedgerBalance());

        CurrencyModel currencyModel = new CurrencyModel();
        currencyModel.setId(currencyResponse.getId());
        currencyModel.setCode(currencyResponse.getCode());
        currencyModel.setName(currencyResponse.getName());
        currencyModel.setSymbol(currencyResponse.getSymbol());
        currencyModel.setDecimalPlaces(currencyResponse.getDecimalPlaces());

        AccountProductModel productModel = new AccountProductModel();
        productModel.setId(accountProductResponse.getId());
        productModel.setProductCode(accountProductResponse.getProductCode());
        productModel.setName(accountProductResponse.getName());
        productModel.setDescription(accountProductResponse.getDescription());
        productModel.setType(accountProductResponse.getProductType());
        productModel.setStatus(accountProductResponse.getStatus());

        BranchModel branchModel = new BranchModel();
        branchModel.setId(branchResponse.getId());
        branchModel.setName(branchResponse.getName());
        branchModel.setBranchCode(branchResponse.getBranchCode());
        branchModel.setPhone(branchResponse.getPhone());
        branchModel.setAddress(branchResponse.getAddress());

        AccountOverviewModel accountOverview = new AccountOverviewModel();
        accountOverview.setAccountId(account.getId());
        accountOverview.setAccountNumber(account.getAccountNumber());
        accountOverview.setStatus(account.getStatus());
        accountOverview.setOpenedAt(account.getOpenedAt());
        accountOverview.setBalance(balanceModel);
        accountOverview.setCurrency(currencyModel);
        accountOverview.setAccountProduct(productModel);
        accountOverview.setBranch(branchModel);

        return accountOverview;
    }
}
