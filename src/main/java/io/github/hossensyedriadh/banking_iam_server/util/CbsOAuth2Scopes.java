package io.github.hossensyedriadh.banking_iam_server.util;

import java.util.Set;

public final class CbsOAuth2Scopes {
    private CbsOAuth2Scopes() {
    }

    public static final String BALANCE_READ = "cbs.balance.read";
    public static final String ACCOUNT_READ = "cbs.account.read";
    public static final String CARD_READ = "cbs.card.read";
    public static final String CARD_PIN_VERIFY = "cbs.card-pin.verify";
    public static final String CURRENCY_READ = "cbs.currency.read";
    public static final String ACCOUNT_PRODUCT_READ = "cbs.account-product.read";
    public static final String WITHDRAW_CREATE = "cbs.withdraw.create";
    public static final String REVERSAL_CREATE = "cbs.reversal.create";
    public static final String TRANSACTIONS_SEARCH_READ = "cbs.transactions-search.read";
    public static final String TRANSACTION_READ = "cbs.transaction.read";

    public static Set<String> all() {
        return Set.of(BALANCE_READ, ACCOUNT_READ, CARD_READ, CARD_PIN_VERIFY, CURRENCY_READ, ACCOUNT_PRODUCT_READ,
                WITHDRAW_CREATE, REVERSAL_CREATE, TRANSACTIONS_SEARCH_READ, TRANSACTION_READ);
    }
}
