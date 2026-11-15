package io.github.hossensyedriadh.cbs.util;

public final class OAuth2Scopes {
    private OAuth2Scopes() {
    }

    public static final String BALANCE_READ = "SCOPE_cbs.balance.read";
    public static final String ACCOUNT_LIST = "SCOPE_cbs.account.list";
    public static final String ACCOUNT_READ = "SCOPE_cbs.account.read";
    public static final String BRANCH_READ = "SCOPE_cbs.branch.read";
    public static final String CARD_READ = "SCOPE_cbs.card.read";
    public static final String CARD_PIN_VERIFY = "SCOPE_cbs.card-pin.verify";
    public static final String CURRENCY_READ = "SCOPE_cbs.currency.read";
    public static final String ACCOUNT_PRODUCT_READ = "SCOPE_cbs.account-product.read";
    public static final String WITHDRAW_CREATE = "SCOPE_cbs.withdraw.create";
    public static final String REVERSAL_CREATE = "SCOPE_cbs.reversal.create";
    public static final String TRANSACTIONS_SEARCH_READ = "SCOPE_cbs.transactions-search.read";
    public static final String TRANSACTION_READ = "SCOPE_cbs.transaction.read";
}
