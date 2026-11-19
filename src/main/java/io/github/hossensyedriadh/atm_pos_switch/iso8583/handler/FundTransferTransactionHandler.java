package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCurrencyResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingFundTransferResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.AccountLookupService;
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
public class FundTransferTransactionHandler implements TransactionHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final CardAccountLookupService cardAccountLookupService;
    private final AccountLookupService accountLookupService;
    private final CurrencyLookupService currencyLookupService;
    private final SwitchTransactionService switchTransactionService;
    private final CoreBankingService coreBankingService;
    private final CardAccountValidator cardAccountValidator;

    @Autowired
    public FundTransferTransactionHandler(IsoResponseFactory isoResponseFactory, CardAccountLookupService cardAccountLookupService,
                                          AccountLookupService accountLookupService, CurrencyLookupService currencyLookupService,
                                          SwitchTransactionService switchTransactionService, CoreBankingService coreBankingService,
                                          CardAccountValidator cardAccountValidator) {
        this.isoResponseFactory = isoResponseFactory;
        this.cardAccountLookupService = cardAccountLookupService;
        this.accountLookupService = accountLookupService;
        this.currencyLookupService = currencyLookupService;
        this.switchTransactionService = switchTransactionService;
        this.coreBankingService = coreBankingService;
        this.cardAccountValidator = cardAccountValidator;
    }

    @Override
    public String processingCode() {
        return ProcessingCodes.FUND_TRANSFER;
    }

    @Override
    public ISOMsg handle(ISOMsg request) {
        SwitchTransactionSummary transaction = null;

        try {
            String pan = request.getString(2);
            String destinationAccountNo = request.getString(103);
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

            if (cardAccount.accountNumber().equals(destinationAccountNo)) {
                log.warn("Fund transfer source and destination accounts [{}, {}] can not be same, fund transfer failed", cardAccount.accountNumber(), destinationAccountNo);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_TRANSACTION);
            }

            String transactionCurrencyCode = request.getString(49);

            CoreBankingCurrencyResponse currencyResponse = this.currencyLookupService.lookupCurrency(cardAccount.currencyId());

            if (!currencyResponse.isActive()) {
                log.warn("Currency is not active: currencyCode={}", transactionCurrencyCode);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CURRENCY);
            }

            if (!currencyResponse.getNumericCode().equals(transactionCurrencyCode)) {
                log.warn("Currency code mismatch: cbsCurrencyCode={}, switchTransactionCurrencyCode={}", currencyResponse.getNumericCode(), transactionCurrencyCode);
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CURRENCY);
            }

            CoreBankingAccountResponse destinationAccount = this.accountLookupService.lookupAccount(destinationAccountNo);

            if (destinationAccount.getStatus() != AccountStatus.ACTIVE) {
                log.warn("Destination account [ID: {}] is not active, fund transfer not possible", destinationAccount.getId());
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_ACCOUNT);
            }

            //fixme: check on this after FX conversion impl
            if (!cardAccount.currencyId().equals(destinationAccount.getCurrencyId())) {
                log.warn("For fund transfer, Source and destination accounts [ID: {}, {}] currencies must be same", cardAccount.accountId(), destinationAccount.getId());
                return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CURRENCY);
            }

            this.switchTransactionService.updateCbsAccountAndCurrencyIds(transaction.id(), cardAccount.accountId(), cardAccount.currencyId());

            String idempotencyKey = Long.toString(transaction.id());

            CoreBankingFundTransferResult result = this.coreBankingService.transfer(transaction, cardAccount.accountId(), destinationAccount.getId(),
                    cardAccount.currencyId(), idempotencyKey);

            if (result.cbsTransactionId() != null) {
                this.switchTransactionService.updateCbsTransactionIds(transaction.id(), result.cbsTransactionId(), null);
            }

            String responseCode = result.successful() ? ResponseCodes.APPROVED : result.responseCode();

            return this.isoResponseFactory.createResponse(request, responseCode);
        } catch (CoreBankingCommunicationException e) {
            log.error("Unable to determine fund transfer result because CBS communication failed", e);

            if (transaction != null) {
                this.switchTransactionService.markFailed(transaction.id(), TransactionStatus.UNKNOWN);
            }

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        } catch (CoreBankingException e) {
            log.error("CBS lookup or processing error before a confirmed fund transfer", e);

            if (transaction != null) {
                this.switchTransactionService.markFailed(transaction.id(), TransactionStatus.FAILED);
            }

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }
    }
}
