package io.github.hossensyedriadh.banking_iam_server.util;

import java.util.Set;

public final class InternetBankingOAuth2Scopes {
    private InternetBankingOAuth2Scopes() {
    }

    public static final String BALANCE_READ = "ibanking.balance.read";
    public static final String ACCOUNT_INFO_LIST = "ibanking.account-info.list";
    public static final String ACCOUNT_INFO_READ = "ibanking.account-info.read";
    public static final String BRANCH_INFO_READ = "ibanking.branch-info.read";
    public static final String CARD_INFO_READ = "ibanking.card-info.read";
    public static final String CARD_PIN_VERIFY = "ibanking.card-pin.verify";
    public static final String CURRENCY_INFO_READ = "ibanking.currency-info.read";
    public static final String ACCOUNT_PRODUCT_INFO_READ = "ibanking.account-product-info.read";
    public static final String TRANSFER_CREATE = "ibanking.transfer.create";
    public static final String TRANSACTIONS_SEARCH_READ = "ibanking.transactions-search.read";
    public static final String TRANSACTION_READ = "ibanking.transaction.read";

    public static Set<String> forUserClient() {
        return Set.of(BALANCE_READ, ACCOUNT_INFO_LIST, ACCOUNT_INFO_READ, BRANCH_INFO_READ, CARD_INFO_READ, CARD_PIN_VERIFY,
                CURRENCY_INFO_READ, ACCOUNT_PRODUCT_INFO_READ, TRANSFER_CREATE, TRANSACTIONS_SEARCH_READ, TRANSACTION_READ);
    }

    public static Set<String> forMachineClient() {
        return Set.of(TRANSFER_CREATE, TRANSACTION_READ);
    }
}
