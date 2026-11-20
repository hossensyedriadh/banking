package io.github.hossensyedriadh.banking_api_gateway.endpoint.v1;

public final class InternetBankingEndpoints {
    private InternetBankingEndpoints() {
    }

    public static final String BASE = "/api/v1/internet-banking";

    public static final String ACCOUNT_BALANCE = BASE + "/accounts/{id}/balance";
    public static final String ACCOUNT_BALANCE_REWRITE = BASE + "/accounts/(?<id>[^/]+)/balance";

    public static final String ACCOUNT_INFO = BASE + "/accounts/{id}";
    public static final String ACCOUNT_INFO_REWRITE = BASE + "/accounts/(?<id>[^/]+)";

    public static final String ACCOUNT_LIST = BASE + "/accounts/customer/{customerId}/list";
    public static final String ACCOUNT_LIST_REWRITE = BASE + "/accounts/customer/(?<customerId>[^/]+)/list";

    public static final String ACCOUNT_PRODUCT_INFO = BASE + "/account-products/{id}";
    public static final String ACCOUNT_PRODUCT_INFO_REWRITE = BASE + "/account-products/(?<id>[^/]+)";

    public static final String CURRENCY_INFO = BASE + "/currencies/{id}";
    public static final String CURRENCY_INFO_REWRITE = BASE + "/currencies/(?<id>[^/]+)";

    public static final String BRANCH_INFO = BASE + "/branches/{id}";
    public static final String BRANCH_INFO_REWRITE = BASE + "/branches/(?<id>[^/]+)";

    public static final String CARD_INFO_BY_ID = BASE + "/cards/{id}";
    public static final String CARD_INFO_BY_ID_REWRITE = BASE + "/cards/(?<id>[^/]+)";

    public static final String CARD_INFO_BY_PAN = BASE + "/cards/pan/{pan}";
    public static final String CARD_INFO_BY_PAN_REWRITE = BASE + "/cards/pan/(?<pan>[^/]+)";

    public static final String CARD_PIN_VERIFICATION = BASE + "/card-pins/pin-verification";
    public static final String CARD_PIN_VERIFICATION_REWRITE = BASE + "/card-pins/pin-verification";

    public static final String TRANSACTIONS_SEARCH = BASE + "/transactions";
    public static final String TRANSACTIONS_SEARCH_REWRITE = BASE + "/transactions";

    public static final String TRANSACTION_BY_ID = BASE + "/transactions/{id}";
    public static final String TRANSACTION_BY_ID_REWRITE = BASE + "/transactions/(?<id>[^/]+)";

    public static final String TRANSACTION_BY_IDEMPOTENCY = BASE + "/transactions/idempotency/{key}";
    public static final String TRANSACTION_BY_IDEMPOTENCY_REWRITE = BASE + "/transactions/idempotency/(?<key>[^/]+)";

    public static final String TRANSACTION_TRANSFER = BASE + "/transactions/transfers";
    public static final String TRANSACTION_TRANSFER_REWRITE = BASE + "/transactions/transfers";

    public static final String TRANSACTION_REVERSAL = BASE + "/transactions/reversals";
    public static final String TRANSACTION_REVERSAL_REWRITE = BASE + "/transactions/reversals";
}
