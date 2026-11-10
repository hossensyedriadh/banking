package io.github.hossensyedriadh.internet_banking.account.service;

import io.github.hossensyedriadh.internet_banking.account.model.AccountBalanceModel;
import io.github.hossensyedriadh.internet_banking.account.model.AccountOverviewModel;
import io.github.hossensyedriadh.internet_banking.account.model.AccountProductModel;
import io.github.hossensyedriadh.internet_banking.account.model.CurrencyModel;
import io.github.hossensyedriadh.internet_banking.http.cbs.CoreBankingSolutionService;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsAccountBalanceResponse;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsAccountProductResponse;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsAccountResponse;
import io.github.hossensyedriadh.internet_banking.http.cbs.model.CbsCurrencyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
    private final CoreBankingSolutionService coreBankingSolutionService;

    @Autowired
    public AccountServiceImpl(CoreBankingSolutionService coreBankingSolutionService) {
        this.coreBankingSolutionService = coreBankingSolutionService;
    }

    @Override
    public List<AccountOverviewModel> overview() {
        List<CbsAccountResponse> bankAccounts = this.coreBankingSolutionService.fetchBankAccounts();

        return bankAccounts.stream().map(account -> {
            CbsAccountBalanceResponse balanceResponse = this.coreBankingSolutionService.fetchAccountBalance(account.getId());
            CbsCurrencyResponse currencyResponse = this.coreBankingSolutionService.fetchCurrencyInfo(account.getCurrencyId());
            CbsAccountProductResponse accountProductResponse = this.coreBankingSolutionService.fetchAccountProductInfo(account.getProductId());

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

            AccountOverviewModel accountOverview = new AccountOverviewModel();
            accountOverview.setAccountId(account.getId());
            accountOverview.setAccountNumber(account.getAccountNumber());
            accountOverview.setStatus(account.getStatus());
            accountOverview.setOpenedAt(account.getOpenedAt());
            accountOverview.setBalance(balanceModel);
            accountOverview.setCurrency(currencyModel);
            accountOverview.setAccountProduct(productModel);

            return accountOverview;
        }).toList();
    }
}
