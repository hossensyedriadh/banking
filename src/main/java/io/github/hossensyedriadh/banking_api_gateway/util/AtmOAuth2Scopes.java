package io.github.hossensyedriadh.banking_api_gateway.util;

public final class AtmOAuth2Scopes {
    private AtmOAuth2Scopes() {
    }

    public static final String ATM_BALANCE_READ = "SCOPE_atm.balance.read";
    public static final String ATM_ACCOUNT_INFO_READ = "SCOPE_atm.account-info.read";
    public static final String ATM_CARD_INFO_READ = "SCOPE_atm.card-info.read";
    public static final String ATM_CARD_PIN_VERIFY = "SCOPE_atm.card-pin.verify";
    public static final String ATM_CURRENCY_INFO_READ = "SCOPE_atm.currency-info.read";
    public static final String ATM_ACCOUNT_PRODUCT_INFO_READ = "SCOPE_atm.account-product-info.read";
    public static final String ATM_WITHDRAW_CREATE = "SCOPE_atm.withdraw.create";
    public static final String ATM_REVERSAL_CREATE = "SCOPE_atm.reversal.create";
    public static final String ATM_TRANSACTIONS_SEARCH_READ = "SCOPE_atm.transactions-search.read";
    public static final String ATM_TRANSACTION_READ = "SCOPE_atm.transaction.read";
}
