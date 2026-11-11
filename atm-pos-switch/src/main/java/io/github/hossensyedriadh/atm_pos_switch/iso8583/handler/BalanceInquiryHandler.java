package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountBalanceResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingAccountProductResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCurrencyResponse;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.AccountProductLookupService;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.BalanceLookupService;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CardAccountLookupService;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CurrencyLookupService;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.encoder.AdditionalAmountEncoder;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.validator.CardAccountValidator;
import io.github.hossensyedriadh.atm_pos_switch.util.BalanceAmountType;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Log4j2
@Service
public class BalanceInquiryHandler implements TransactionHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final AdditionalAmountEncoder additionalAmountEncoder;
    private final CardAccountValidator cardAccountValidator;
    private final CardAccountLookupService cardAccountLookupService;
    private final AccountProductLookupService accountProductLookupService;
    private final CurrencyLookupService currencyLookupService;
    private final BalanceLookupService balanceLookupService;

    @Autowired
    public BalanceInquiryHandler(IsoResponseFactory isoResponseFactory, AdditionalAmountEncoder additionalAmountEncoder,
                                 CardAccountValidator cardAccountValidator, CardAccountLookupService cardAccountLookupService,
                                 AccountProductLookupService accountProductLookupService, CurrencyLookupService currencyLookupService,
                                 BalanceLookupService balanceLookupService) {
        this.isoResponseFactory = isoResponseFactory;
        this.additionalAmountEncoder = additionalAmountEncoder;
        this.cardAccountValidator = cardAccountValidator;
        this.cardAccountLookupService = cardAccountLookupService;
        this.accountProductLookupService = accountProductLookupService;
        this.currencyLookupService = currencyLookupService;
        this.balanceLookupService = balanceLookupService;
    }

    @Override
    public String processingCode() {
        return ProcessingCodes.BALANCE_INQUIRY;
    }

    @Override
    public ISOMsg handle(ISOMsg request) {
        String pan = request.getString(2);

        if (pan == null || !pan.matches("\\d{12,19}")) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CARD);
        }

        CoreBankingCardAccountAggregateResult aggregateResult = this.cardAccountLookupService.lookupCardAccount(pan);

        String validationResult = this.cardAccountValidator.validate(aggregateResult);

        if (!Objects.equals(validationResult, ResponseCodes.APPROVED)) {
            return this.isoResponseFactory.createResponse(request, validationResult);
        }

        CoreBankingCurrencyResponse currencyResponse = this.currencyLookupService.lookupCurrency(aggregateResult.currencyId());

        if (currencyResponse == null) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_CURRENCY);
        }

        CoreBankingAccountProductResponse accountProductResponse = this.accountProductLookupService.lookupAccountProduct(aggregateResult.accountProductId());

        if (accountProductResponse == null) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }

        CoreBankingAccountBalanceResponse balanceResponse = this.balanceLookupService.lookupBalance(aggregateResult.accountId());

        if (balanceResponse == null) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }

        ISOMsg response = this.isoResponseFactory.createResponse(request, ResponseCodes.APPROVED);

        BigDecimal availableBalance = balanceResponse.getAvailableBalance();
        char sign = availableBalance.signum() < 0 ? 'D' : 'C';
        long amountMinorUnits = availableBalance.abs().movePointRight(currencyResponse.getDecimalPlaces()).longValueExact();

        String balance = this.additionalAmountEncoder.encode(accountProductResponse.getProductType().getDe54Code(),
                BalanceAmountType.AVAILABLE.getDe54Code(), currencyResponse.getNumericCode(), sign, amountMinorUnits);

        response.set(54, balance);

        return response;
    }
}
