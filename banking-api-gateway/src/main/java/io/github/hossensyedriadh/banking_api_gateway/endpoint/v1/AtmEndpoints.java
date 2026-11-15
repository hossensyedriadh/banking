package io.github.hossensyedriadh.banking_api_gateway.endpoint.v1;

public final class AtmEndpoints {
    private AtmEndpoints() {
    }

    public static final String BASE = "/api/v1/atm";

    public static final String ACCOUNT_BALANCE = BASE + "/accounts/{accountId}/cards/{cardId}/balance";
    public static final String ACCOUNT_BALANCE_REWRITE = BASE + "/accounts/(?<accountId>[^/]+)/cards/(?<cardId>[^/]+)/balance";

    public static final String ACCOUNT_INFO = BASE + "/accounts/{id}";
    public static final String ACCOUNT_INFO_REWRITE = BASE + "/accounts/(?<id>[^/]+)";

    public static final String ACCOUNT_PRODUCT_INFO = BASE + "/account-products/{id}";
    public static final String ACCOUNT_PRODUCT_INFO_REWRITE = BASE + "/account-products/(?<id>[^/]+)";

    public static final String CURRENCY_INFO = BASE + "/currencies/{id}";
    public static final String CURRENCY_INFO_REWRITE = BASE + "/currencies/(?<id>[^/]+)";

    public static final String CARD_INFO_BY_PAN = BASE + "/cards/{pan}";
    public static final String CARD_INFO_BY_PAN_REWRITE = BASE + "/cards/(?<pan>[^/]+)";

    public static final String CARD_PIN_VERIFICATION = BASE + "/card-pins/pin-verification";
    public static final String CARD_PIN_VERIFICATION_REWRITE = BASE + "/card-pins/pin-verification";

    public static final String TRANSACTIONS_SEARCH = BASE + "/transactions";
    public static final String TRANSACTIONS_SEARCH_REWRITE = BASE + "/transactions";

    public static final String TRANSACTION_BY_IDEMPOTENCY = BASE + "/transactions/idempotency/{key}";
    public static final String TRANSACTION_BY_IDEMPOTENCY_REWRITE = BASE + "/transactions/idempotency/(?<key>[^/]+)";

    public static final String TRANSACTION_WITHDRAWAL = BASE + "/transactions/withdrawals";
    public static final String TRANSACTION_WITHDRAWAL_REWRITE = BASE + "/transactions/withdrawals";

    public static final String TRANSACTION_REVERSAL = BASE + "/transactions/reversals";
    public static final String TRANSACTION_REVERSAL_REWRITE = BASE + "/transactions/reversals";
}
