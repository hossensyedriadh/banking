package io.github.hossensyedriadh.banking_iam_server.util;

import java.util.Set;

public final class AtmOAuth2Scopes {
    private AtmOAuth2Scopes() {
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

    public static Set<String> all() {
        return Set.of(ATM_BALANCE_READ, ATM_ACCOUNT_INFO_READ, ATM_CARD_INFO_READ, ATM_CARD_PIN_VERIFY,
                ATM_CURRENCY_INFO_READ, ATM_ACCOUNT_PRODUCT_INFO_READ, ATM_WITHDRAW_CREATE, ATM_REVERSAL_CREATE,
                ATM_TRANSACTIONS_SEARCH_READ, ATM_TRANSACTION_READ);
    }
}
