package io.github.hossensyedriadh.banking_api_gateway.security.token;

import io.github.hossensyedriadh.banking_api_gateway.enumerator.ChannelType;
import io.github.hossensyedriadh.banking_api_gateway.util.AtmOAuth2Scopes;
import io.github.hossensyedriadh.banking_api_gateway.util.CbsOAuth2Scopes;
import io.github.hossensyedriadh.banking_api_gateway.util.InternetBankingOAuth2Scopes;

import java.util.Map;

public final class TokenExchangeScopeMappings {
    private TokenExchangeScopeMappings() {
    }

    public static final Map<String, String> ATM_TO_CBS = Map.of(
            AtmOAuth2Scopes.BALANCE_READ, CbsOAuth2Scopes.BALANCE_READ,
            AtmOAuth2Scopes.ACCOUNT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_READ,
            AtmOAuth2Scopes.CARD_INFO_READ, CbsOAuth2Scopes.CARD_READ,
            AtmOAuth2Scopes.CARD_PIN_VERIFY, CbsOAuth2Scopes.CARD_PIN_VERIFY,
            AtmOAuth2Scopes.CURRENCY_INFO_READ, CbsOAuth2Scopes.CURRENCY_READ,
            AtmOAuth2Scopes.ACCOUNT_PRODUCT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_PRODUCT_READ,
            AtmOAuth2Scopes.WITHDRAW_CREATE, CbsOAuth2Scopes.WITHDRAW_CREATE,
            AtmOAuth2Scopes.REVERSAL_CREATE, CbsOAuth2Scopes.REVERSAL_CREATE,
            AtmOAuth2Scopes.TRANSACTIONS_SEARCH_READ, CbsOAuth2Scopes.TRANSACTIONS_SEARCH_READ,
            AtmOAuth2Scopes.TRANSACTION_READ, CbsOAuth2Scopes.TRANSACTION_READ
    );

    public static final Map<String, String> INTERNET_BANKING_TO_CBS = Map.of(
            InternetBankingOAuth2Scopes.BALANCE_READ, CbsOAuth2Scopes.BALANCE_READ,
            InternetBankingOAuth2Scopes.ACCOUNT_INFO_LIST, CbsOAuth2Scopes.ACCOUNT_LIST,
            InternetBankingOAuth2Scopes.ACCOUNT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_READ,
            InternetBankingOAuth2Scopes.CARD_INFO_READ, CbsOAuth2Scopes.CARD_READ,
            InternetBankingOAuth2Scopes.CURRENCY_INFO_READ, CbsOAuth2Scopes.CURRENCY_READ,
            InternetBankingOAuth2Scopes.ACCOUNT_PRODUCT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_PRODUCT_READ,
            InternetBankingOAuth2Scopes.TRANSFER_CREATE, CbsOAuth2Scopes.TRANSFER_CREATE,
            InternetBankingOAuth2Scopes.REVERSAL_CREATE, CbsOAuth2Scopes.REVERSAL_CREATE,
            InternetBankingOAuth2Scopes.TRANSACTIONS_SEARCH_READ, CbsOAuth2Scopes.TRANSACTIONS_SEARCH_READ,
            InternetBankingOAuth2Scopes.TRANSACTION_READ, CbsOAuth2Scopes.TRANSACTION_READ
    );

    public static Map<String, String> forChannel(ChannelType channel) {
        return switch (channel) {
            case SWITCH -> ATM_TO_CBS;
            case INTERNET_BANKING -> INTERNET_BANKING_TO_CBS;
        };
    }
}
