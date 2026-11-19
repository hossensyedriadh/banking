package io.github.hossensyedriadh.banking_api_gateway.endpoint.v1;

public final class CbsEndpoints {
    private CbsEndpoints() {
    }

    public static final String BASE = "/api";

    public static final String ACCOUNT_BALANCE = BASE + "/accounts/v1/balance/{id}";
    public static final String ACCOUNT_BALANCE_REWRITE = BASE + "/accounts/v1/balance/${id}";

    public static final String ATM_ACCOUNT_BALANCE = BASE + "/accounts/v1/balance/{accountId}/card/{cardId}";
    public static final String ATM_ACCOUNT_BALANCE_REWRITE = BASE + "/accounts/v1/balance/${accountId}/card/${cardId}";

    public static final String ACCOUNT_LIST = BASE + "/accounts/v1/customer/{customerId}/active";
    public static final String ACCOUNT_LIST_REWRITE = BASE + "/accounts/v1/customer/${customerId}/active";

    public static final String ACCOUNT_INFO = BASE + "/accounts/v1/{id}";
    public static final String ACCOUNT_INFO_REWRITE = BASE + "/accounts/v1/${id}";

    public static final String ACCOUNT_INFO_BY_ACCOUNT_NUMBER = BASE + "/accounts/v1/account-number/{number}";
    public static final String ACCOUNT_INFO_BY_ACCOUNT_NUMBER_REWRITE = BASE + "/accounts/v1/account-number/${number}";

    public static final String PRINCIPAL_ACCOUNT_INFO = BASE + "/accounts/v1/principal/{id}";
    public static final String PRINCIPAL_ACCOUNT_INFO_REWRITE = BASE + "/accounts/v1/principal/${id}";

    public static final String ACCOUNT_PRODUCT_INFO = BASE + "/account-products/v1/{id}";
    public static final String ACCOUNT_PRODUCT_INFO_REWRITE = BASE + "/account-products/v1/${id}";

    public static final String CURRENCY_INFO = BASE + "/currencies/v1/{id}";
    public static final String CURRENCY_INFO_REWRITE = BASE + "/currencies/v1/${id}";

    public static final String BRANCH_INFO = BASE + "/branches/v1/{id}";
    public static final String BRANCH_INFO_REWRITE = BASE + "/branches/v1/${id}";

    public static final String CARD_INFO_BY_PAN = BASE + "/cards/v1/pan/{pan}";
    public static final String CARD_INFO_BY_PAN_REWRITE = BASE + "/cards/v1/pan/${pan}";

    public static final String CARD_INFO_BY_ID = BASE + "/cards/v1/{id}";
    public static final String CARD_INFO_BY_ID_REWRITE = BASE + "/cards/v1/${id}";

    public static final String CARD_PIN_VERIFICATION = BASE + "/card-pins/v1/verification";
    public static final String CARD_PIN_VERIFICATION_REWRITE = BASE + "/card-pins/v1/verification";

    public static final String TRANSACTIONS_SEARCH = BASE + "/transactions/v1";
    public static final String TRANSACTIONS_SEARCH_REWRITE = BASE + "/transactions/v1";

    public static final String TRANSACTION_BY_IDEMPOTENCY = BASE + "/transactions/v1/idempotency/{key}";
    public static final String TRANSACTION_BY_IDEMPOTENCY_REWRITE = BASE + "/transactions/v1/idempotency/${key}";

    public static final String TRANSACTION_WITHDRAWAL = BASE + "/transactions/v1/withdrawals";
    public static final String TRANSACTION_WITHDRAWAL_REWRITE = BASE + "/transactions/v1/withdrawals";

    public static final String TRANSACTION_TRANSFER = BASE + "/transactions/v1/transfers";
    public static final String TRANSACTION_TRANSFER_REWRITE = BASE + "/transactions/v1/transfers";

    public static final String TRANSACTION_REVERSAL = BASE + "/transactions/v1/reversals";
    public static final String TRANSACTION_REVERSAL_REWRITE = BASE + "/transactions/v1/reversals";
}
