package io.github.hossensyedriadh.banking_iam_server.util;

public final class OAuth2Scopes {
    private OAuth2Scopes() {
    }

    public static final String ATM_BALANCE_READ = "atm.balance.read";
    public static final String ATM_ACCOUNT_INFO_READ = "atm.account-info.read";
    public static final String ATM_CARD_INFO_READ = "atm.card-info.read";
    public static final String ATM_CARD_PIN_VERIFY = "atm.card-pin.verify";
    public static final String ATM_CURRENCY_INFO_READ = "atm.currency-info.read";
    public static final String ATM_ACCOUNT_PRODUCT_INFO_READ = "atm.account-product-info.read";
    public static final String ATM_WITHDRAW_CREATE = "atm.withdraw.create";
    public static final String ATM_REVERSAL_CREATE = "atm.reversal.create";
    public static final String ATM_TRANSACTIONS_SEARCH_READ = "atm.transactions-search.read";
    public static final String ATM_TRANSACTION_READ = "atm.transaction.read";
}
