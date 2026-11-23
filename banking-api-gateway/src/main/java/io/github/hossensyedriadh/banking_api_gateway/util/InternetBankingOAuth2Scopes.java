package io.github.hossensyedriadh.banking_api_gateway.util;

public final class InternetBankingOAuth2Scopes {
    private InternetBankingOAuth2Scopes() {
    }

    public static final String BALANCE_READ = "SCOPE_ibanking.balance.read";
    public static final String ACCOUNT_INFO_LIST = "SCOPE_ibanking.account-info.list";
    public static final String ACCOUNT_INFO_READ = "SCOPE_ibanking.account-info.read";
    public static final String BRANCH_INFO_READ = "SCOPE_ibanking.branch-info.read";
    public static final String CARD_INFO_READ = "SCOPE_ibanking.card-info.read";
    public static final String CARD_PIN_VERIFY = "SCOPE_ibanking.card-pin.verify";
    public static final String CURRENCY_INFO_READ = "SCOPE_ibanking.currency-info.read";
    public static final String ACCOUNT_PRODUCT_INFO_READ = "SCOPE_ibanking.account-product-info.read";
    public static final String TRANSFER_CREATE = "SCOPE_ibanking.transfer.create";
    public static final String TRANSACTIONS_SEARCH_READ = "SCOPE_ibanking.transactions-search.read";
    public static final String TRANSACTION_READ = "SCOPE_ibanking.transaction.read";
}
