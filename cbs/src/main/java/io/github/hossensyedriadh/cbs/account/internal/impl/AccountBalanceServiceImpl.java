package io.github.hossensyedriadh.cbs.account.internal.impl;

import io.github.hossensyedriadh.cbs.account.api.model.AccountBalanceView;
import io.github.hossensyedriadh.cbs.account.api.service.AccountBalanceService;
import io.github.hossensyedriadh.cbs.account.api.service.AccountHoldService;
import io.github.hossensyedriadh.cbs.aspect.annotation.AuthorizeAccountAccess;
import io.github.hossensyedriadh.cbs.card.api.enumerator.CardStatus;
import io.github.hossensyedriadh.cbs.card.api.model.CardView;
import io.github.hossensyedriadh.cbs.card.api.service.CardService;
import io.github.hossensyedriadh.cbs.exception.ApiResourceException;
import io.github.hossensyedriadh.cbs.ledger.api.model.SubLedgerAccountView;
import io.github.hossensyedriadh.cbs.ledger.api.service.LedgerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Log4j2
@Service
@Transactional(readOnly = true)
public class AccountBalanceServiceImpl implements AccountBalanceService {
    private final LedgerService ledgerService;
    private final CardService cardService;
    private final AccountHoldService accountHoldService;

    @Autowired
    public AccountBalanceServiceImpl(LedgerService ledgerService, CardService cardService, AccountHoldService accountHoldService) {
        this.ledgerService = ledgerService;
        this.cardService = cardService;
        this.accountHoldService = accountHoldService;
    }

    @Override
    @AuthorizeAccountAccess(accountIdParam = "accountId")
    public AccountBalanceView getBalance(Long accountId) {
        return this.prepareBalanceResponse(accountId);
    }

    @Override
    @AuthorizeAccountAccess(accountIdParam = "accountId", cardIdParam = "cardId")
    public AccountBalanceView getBalance(Long accountId, Long cardId) {
        CardView card = this.cardService.getCard(cardId).orElseThrow(() -> {
            log.warn("Card not found with ID: {}", cardId);
            return new ApiResourceException("Card not found", HttpStatus.NOT_FOUND);
        });

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card [ID: {}] is not active", cardId);
            throw new ApiResourceException("Card is not active", HttpStatus.BAD_REQUEST);
        }

        if (!card.getAccountId().equals(accountId)) {
            log.warn("Card [ID: {}] is not associated with requested account [ID: {}]", cardId, accountId);
            throw new ApiResourceException("Card is not associated with this account", HttpStatus.FORBIDDEN);
        }

        return this.prepareBalanceResponse(card.getAccountId());
    }

    private AccountBalanceView prepareBalanceResponse(Long accountId) {
        BigDecimal ledgerBalance = this.ledgerService.getSubLedgerAccountByAccountId(accountId).map(SubLedgerAccountView::getBalance).orElse(BigDecimal.ZERO);
        BigDecimal heldAmount = this.accountHoldService.getActiveHoldSum(accountId);
        BigDecimal availableBalance = ledgerBalance.subtract(heldAmount);

        return new AccountBalanceView(accountId, ledgerBalance, heldAmount, availableBalance);
    }
}
