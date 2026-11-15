package io.github.hossensyedriadh.cbs.authorization.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.api.model.AccountView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountService;
import io.github.hossensyedriadh.cbs.authorization.api.service.BankingAuthorizationService;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.service.CardService;
import io.github.hossensyedriadh.cbs.customer.api.service.CustomerIdentityService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@Transactional(readOnly = true)
class BankingAuthorizationServiceImpl implements BankingAuthorizationService {
    private final CustomerIdentityService customerIdentityService;
    private final AccountService accountService;
    private final CardService cardService;

    @Autowired
    public BankingAuthorizationServiceImpl(CustomerIdentityService customerIdentityService, AccountService accountService,
                                           CardService cardService) {
        this.customerIdentityService = customerIdentityService;
        this.accountService = accountService;
        this.cardService = cardService;
    }

    @Override
    public void authorizeAccountAccess(Long accountId) {
        Long customerId = this.customerIdentityService.getAuthenticatedCustomerId();

        this.accountService.getActiveAccountByAccountIdAndCustomerId(accountId, customerId).orElseThrow(() -> {
            log.warn("Account not found with ID: {}", accountId);
            return new ApiResourceException("Account not found", HttpStatus.NOT_FOUND);
        });
    }

    @Override
    public void authorizeCardAccess(Long accountId, Long cardId) {
        CardView card = this.cardService.getCard(cardId).orElseThrow(() -> {
            log.warn("Card not found with ID: {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.NOT_FOUND);
        });

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card [ID: {}] is not active, can not authorize action", cardId);
            throw new ApiResourceException("Card is not active", HttpStatus.BAD_REQUEST);
        }

        if (!card.getAccountId().equals(accountId)) {
            log.warn("Card [ID: {}] is not associated with the request account [ID: {}]", cardId, accountId);
            throw new ApiResourceException("Card not associated with account", HttpStatus.FORBIDDEN);
        }

        AccountView account = this.accountService.getAccount(accountId).orElseThrow(() -> {
            log.warn("Account not found with ID: {}", accountId);
            return new ApiResourceException("Account not found", HttpStatus.NOT_FOUND);
        });

        if (account.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account [ID: {}] is not active", accountId);
            throw new ApiResourceException("Account is not active", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Long getAuthenticatedCustomerId() {
        return this.customerIdentityService.getAuthenticatedCustomerId();
    }
}
