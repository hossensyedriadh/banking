package io.github.hossensyedriadh.banking_api_gateway.security.token;

import io.github.hossensyedriadh.banking_api_gateway.util.AtmOAuth2Scopes;
import io.github.hossensyedriadh.banking_api_gateway.util.CbsOAuth2Scopes;

import java.util.Map;

public final class TokenExchangeScopeMappings {
    private TokenExchangeScopeMappings() {
    }

    public static final Map<String, String> ATM_TO_CBS = Map.of(
            AtmOAuth2Scopes.ATM_BALANCE_READ,
            CbsOAuth2Scopes.BALANCE_READ,

            AtmOAuth2Scopes.ATM_ACCOUNT_INFO_READ,
            CbsOAuth2Scopes.ACCOUNT_READ,

            AtmOAuth2Scopes.ATM_CARD_INFO_READ,
            CbsOAuth2Scopes.CARD_READ,

            AtmOAuth2Scopes.ATM_CARD_PIN_VERIFY,
            CbsOAuth2Scopes.CARD_PIN_VERIFY,

            AtmOAuth2Scopes.ATM_CURRENCY_INFO_READ,
            CbsOAuth2Scopes.CURRENCY_READ,

            AtmOAuth2Scopes.ATM_ACCOUNT_PRODUCT_INFO_READ,
            CbsOAuth2Scopes.ACCOUNT_PRODUCT_READ,

            AtmOAuth2Scopes.ATM_WITHDRAW_CREATE,
            CbsOAuth2Scopes.WITHDRAW_CREATE,

            AtmOAuth2Scopes.ATM_REVERSAL_CREATE,
            CbsOAuth2Scopes.REVERSAL_CREATE,

            AtmOAuth2Scopes.ATM_TRANSACTIONS_SEARCH_READ,
            CbsOAuth2Scopes.TRANSACTIONS_SEARCH_READ,

            AtmOAuth2Scopes.ATM_TRANSACTION_READ,
            CbsOAuth2Scopes.TRANSACTION_READ
    );
}
