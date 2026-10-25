package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.enumerator.CardPinVerificationResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardAccountAggregateResult;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.model.CoreBankingCardPinVerificationRequest;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CardAccountLookupService;
import io.github.hossensyedriadh.atm_pos_switch.corebanking.api.service.CardPinVerificationService;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.validator.CardAccountValidator;
import io.github.hossensyedriadh.atm_pos_switch.service.AtmSessionService;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
public class PinVerificationHandler implements TransactionHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final CardAccountLookupService cardAccountLookupService;
    private final CardPinVerificationService cardPinVerificationService;
    private final CardAccountValidator cardAccountValidator;
    private final AtmSessionService atmSessionService;

    @Autowired
    public PinVerificationHandler(IsoResponseFactory isoResponseFactory, CardAccountLookupService cardAccountLookupService,
                                  CardPinVerificationService cardPinVerificationService, CardAccountValidator cardAccountValidator,
                                  AtmSessionService atmSessionService) {
        this.isoResponseFactory = isoResponseFactory;
        this.cardAccountLookupService = cardAccountLookupService;
        this.cardPinVerificationService = cardPinVerificationService;
        this.cardAccountValidator = cardAccountValidator;
        this.atmSessionService = atmSessionService;
    }

    @Override
    public String processingCode() {
        return ProcessingCodes.PIN_VERIFICATION;
    }

    @Override
    public ISOMsg handle(ISOMsg request) {
        String pan = request.getString(2);

        CoreBankingCardAccountAggregateResult aggregateResult = this.cardAccountLookupService.lookupCardAccount(pan);

        String validationResult = this.cardAccountValidator.validate(aggregateResult);

        if (!Objects.equals(validationResult, ResponseCodes.APPROVED)) {
            return this.isoResponseFactory.createResponse(request, validationResult);
        }

        CoreBankingCardPinVerificationRequest verificationRequest = new CoreBankingCardPinVerificationRequest(pan,
                request.getBytes(52));

        CardPinVerificationResult verificationResult = this.cardPinVerificationService.verify(verificationRequest);

        if (verificationResult == null) {
            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }

        return switch (verificationResult) {
            case SUCCESS -> this.createSessionResponse(request, pan);
            case INVALID_PIN -> this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_PIN);
            case PIN_BLOCKED -> this.isoResponseFactory.createResponse(request, ResponseCodes.PIN_BLOCKED);
        };
    }

    private ISOMsg createSessionResponse(ISOMsg request, String pan) {
        String token = this.atmSessionService.create(pan, request.getString(41));

        try {
            ISOMsg response = this.isoResponseFactory.createResponse(request, ResponseCodes.APPROVED);
            response.set(62, token);

            return response;
        } catch (Exception e) {
            this.atmSessionService.invalidate(token);
            log.error("Could not attach session token to PIN verification response", e);

            return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
        }
    }
}
