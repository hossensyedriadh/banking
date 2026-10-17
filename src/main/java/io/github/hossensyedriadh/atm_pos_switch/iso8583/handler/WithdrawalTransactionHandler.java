package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCurrencyResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingWithdrawalResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CardAccountLookupService;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CoreBankingService;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CurrencyLookupService;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingCommunicationException;
import io.github.hossensyedriadh.atm_pos_switch.exception.CoreBankingException;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.validator.CardAccountValidator;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionSummary;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class WithdrawalTransactionHandler implements TransactionHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final CardAccountLookupService cardAccountLookupService;
    private final CurrencyLookupService currencyLookupService;
    private final SwitchTransactionService switchTransactionService;
    private final CoreBankingService coreBankingService;
    private final CardAccountValidator cardAccountValidator;

    @Autowired
    public WithdrawalTransactionHandler(IsoResponseFactory isoResponseFactory, CardAccountLookupService cardAccountLookupService,
                                        CurrencyLookupService currencyLookupService, SwitchTransactionService switchTransactionService,
                                        CoreBankingService coreBankingService, CardAccountValidator cardAccountValidator) {
        this.isoResponseFactory = isoResponseFactory;
        this.cardAccountLookupService = cardAccountLookupService;
        this.currencyLookupService = currencyLookupService;
        this.switchTransactionService = switchTransactionService;
        this.coreBankingService = coreBankingService;
        this.cardAccountValidator = cardAccountValidator;
    }

    @Override
    public String processingCode() {
        return ProcessingCodes.CASH_WITHDRAWAL;
    }

    @Override
    public ISOMsg handle(ISOMsg request) {
        SwitchTransactionSummary transaction = null;
        try {
            String pan = request.getString(2);
            String terminalId = request.getString(41);
            String stan = request.getString(11);
            String transmissionDateTime = request.getString(7);

            transaction = this.switchTransactionService.findTransaction(terminalId, stan, transmissionDateTime)
                    .orElse(null);

            if (transaction == null) {
                log.error("Switch transaction not found: terminal={}, stan={}", terminalId, stan);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
            }

            CoreBankingCardAccountAggregateResult cardAccount = this.cardAccountLookupService.lookupCardAccount(pan);

            this.cardAccountValidator.validate(cardAccount);

            String transactionCurrencyCode = request.getString(49);

            CoreBankingCurrencyResponse currencyResponse = this.currencyLookupService.lookupCurrency(cardAccount.currencyId());

            if (!currencyResponse.isActive()) {
                log.error("Currency is not active: currencyCode={}", transactionCurrencyCode);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CURRENCY);
            }

            if (!currencyResponse.getNumericCode().equals(transactionCurrencyCode)) {
                log.error("Currency code mismatch: cbsCurrencyCode={}, switchTransactionCurrencyCode={}", currencyResponse.getNumericCode(), transactionCurrencyCode);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CURRENCY);
            }

            this.switchTransactionService.updateCbsAccountAndCurrencyIds(transaction.id(), cardAccount.accountId(), cardAccount.currencyId());

            String idempotencyKey = Long.toString(transaction.id());

            CoreBankingWithdrawalResult result = this.coreBankingService.withdraw(transaction, cardAccount.accountId(), cardAccount.currencyId(), idempotencyKey);

            if (result.cbsTransactionId() != null) {
                this.switchTransactionService.updateCbsTransactionIds(transaction.id(), result.cbsTransactionId(), null);
            }

            String responseCode = result.successful() ? ResponseCodes.APPROVED : result.responseCode();

            return this.isoResponseFactory.createResponse(request, responseCode);
        } catch (CoreBankingCommunicationException e) {
            log.error("Unable to determine withdrawal result because CBS communication failed", e);

            if (transaction != null) {
                this.switchTransactionService.markFailed(transaction.id(), TransactionStatus.UNKNOWN);
            }

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        } catch (CoreBankingException e) {
            log.error("CBS lookup or processing error before a confirmed withdrawal", e);

            if (transaction != null) {
                this.switchTransactionService.markFailed(transaction.id(), TransactionStatus.FAILED);
            }

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }
    }
}
