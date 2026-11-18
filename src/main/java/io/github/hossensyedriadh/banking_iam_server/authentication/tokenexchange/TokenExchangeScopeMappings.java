package io.github.hossensyedriadh.banking_iam_server.authentication.tokenexchange;

import io.github.hossensyedriadh.banking_iam_server.enumerator.ChannelType;
import io.github.hossensyedriadh.banking_iam_server.util.AtmOAuth2Scopes;
import io.github.hossensyedriadh.banking_iam_server.util.CbsOAuth2Scopes;
import io.github.hossensyedriadh.banking_iam_server.util.InternetBankingOAuth2Scopes;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class TokenExchangeScopeMappings {
    private TokenExchangeScopeMappings() {
    }

    public static final Map<String, String> ATM_TO_CBS = Map.ofEntries(
            Map.entry(AtmOAuth2Scopes.BALANCE_READ, CbsOAuth2Scopes.BALANCE_READ),
            Map.entry(AtmOAuth2Scopes.ACCOUNT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_READ),
            Map.entry(AtmOAuth2Scopes.CARD_INFO_READ, CbsOAuth2Scopes.CARD_READ),
            Map.entry(AtmOAuth2Scopes.CARD_PIN_VERIFY, CbsOAuth2Scopes.CARD_PIN_VERIFY),
            Map.entry(AtmOAuth2Scopes.CURRENCY_INFO_READ, CbsOAuth2Scopes.CURRENCY_READ),
            Map.entry(AtmOAuth2Scopes.ACCOUNT_PRODUCT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_PRODUCT_READ),
            Map.entry(AtmOAuth2Scopes.WITHDRAW_CREATE, CbsOAuth2Scopes.WITHDRAW_CREATE),
            Map.entry(AtmOAuth2Scopes.REVERSAL_CREATE, CbsOAuth2Scopes.REVERSAL_CREATE),
            Map.entry(AtmOAuth2Scopes.TRANSFER_CREATE, CbsOAuth2Scopes.TRANSFER_CREATE),
            Map.entry(AtmOAuth2Scopes.TRANSACTIONS_SEARCH_READ, CbsOAuth2Scopes.TRANSACTIONS_SEARCH_READ),
            Map.entry(AtmOAuth2Scopes.TRANSACTION_READ, CbsOAuth2Scopes.TRANSACTION_READ)
    );

    public static final Map<String, String> INTERNET_BANKING_TO_CBS = Map.ofEntries(
            Map.entry(InternetBankingOAuth2Scopes.BALANCE_READ, CbsOAuth2Scopes.BALANCE_READ),
            Map.entry(InternetBankingOAuth2Scopes.ACCOUNT_INFO_LIST, CbsOAuth2Scopes.ACCOUNT_LIST),
            Map.entry(InternetBankingOAuth2Scopes.ACCOUNT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_READ),
            Map.entry(InternetBankingOAuth2Scopes.BRANCH_INFO_READ, CbsOAuth2Scopes.BRANCH_READ),
            Map.entry(InternetBankingOAuth2Scopes.CARD_INFO_READ, CbsOAuth2Scopes.CARD_READ),
            Map.entry(InternetBankingOAuth2Scopes.CURRENCY_INFO_READ, CbsOAuth2Scopes.CURRENCY_READ),
            Map.entry(InternetBankingOAuth2Scopes.ACCOUNT_PRODUCT_INFO_READ, CbsOAuth2Scopes.ACCOUNT_PRODUCT_READ),
            Map.entry(InternetBankingOAuth2Scopes.TRANSFER_CREATE, CbsOAuth2Scopes.TRANSFER_CREATE),
            Map.entry(InternetBankingOAuth2Scopes.REVERSAL_CREATE, CbsOAuth2Scopes.REVERSAL_CREATE),
            Map.entry(InternetBankingOAuth2Scopes.TRANSACTIONS_SEARCH_READ, CbsOAuth2Scopes.TRANSACTIONS_SEARCH_READ),
            Map.entry(InternetBankingOAuth2Scopes.TRANSACTION_READ, CbsOAuth2Scopes.TRANSACTION_READ)
    );

    public static Set<String> allowedCbsScopes(ChannelType channelType, Set<String> subjectScopes) {
        Map<String, String> scopeMappings = switch (channelType) {
            case SWITCH -> ATM_TO_CBS;
            case INTERNET_BANKING -> INTERNET_BANKING_TO_CBS;
        };

        return subjectScopes.stream().map(scopeMappings::get).filter(java.util.Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
