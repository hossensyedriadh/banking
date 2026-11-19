package io.github.hossensyedriadh.banking_iam_server.util;

import java.util.Set;

public final class AtmOAuth2Scopes {
    private AtmOAuth2Scopes() {
    }

    public static final String BALANCE_READ = "atm.balance.read";
    public static final String ACCOUNT_INFO_READ = "atm.account-info.read";
    public static final String CARD_INFO_READ = "atm.card-info.read";
    public static final String CARD_PIN_VERIFY = "atm.card-pin.verify";
    public static final String CURRENCY_INFO_READ = "atm.currency-info.read";
    public static final String ACCOUNT_PRODUCT_INFO_READ = "atm.account-product-info.read";
    public static final String WITHDRAW_CREATE = "atm.withdraw.create";
    public static final String REVERSAL_CREATE = "atm.reversal.create";
    public static final String TRANSFER_CREATE = "atm.transfer.create";
    public static final String TRANSACTIONS_SEARCH_READ = "atm.transactions-search.read";
    public static final String TRANSACTION_READ = "atm.transaction.read";

    public static Set<String> all() {
        return Set.of(BALANCE_READ, ACCOUNT_INFO_READ, CARD_INFO_READ, CARD_PIN_VERIFY,
                CURRENCY_INFO_READ, ACCOUNT_PRODUCT_INFO_READ, WITHDRAW_CREATE, REVERSAL_CREATE,
                TRANSFER_CREATE, TRANSACTIONS_SEARCH_READ, TRANSACTION_READ);
    }
}
