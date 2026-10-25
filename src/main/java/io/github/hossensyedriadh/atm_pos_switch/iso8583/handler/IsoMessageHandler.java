package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.parser.IsoMessageParser;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.router.TransactionRouter;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.validator.IsoRequestValidator;
import io.github.hossensyedriadh.atm_pos_switch.service.AtmSessionService;
import io.github.hossensyedriadh.atm_pos_switch.terminal.api.service.SwitchTerminalService;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.enumerator.TransactionStatus;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.SwitchTransactionRegistrationResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.model.TransactionCompletionResult;
import io.github.hossensyedriadh.atm_pos_switch.transaction.api.service.SwitchTransactionService;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Log4j2
@Component
public class IsoMessageHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final IsoRequestValidator isoRequestValidator;
    private final IsoMessageParser isoMessageParser;
    private final TransactionRouter transactionRouter;
    private final SwitchTerminalService switchTerminalService;
    private final SwitchTransactionService switchTransactionService;
    private final AtmSessionService atmSessionService;

    @Autowired
    public IsoMessageHandler(IsoResponseFactory isoResponseFactory, IsoRequestValidator isoRequestValidator,
                             IsoMessageParser isoMessageParser, TransactionRouter transactionRouter,
                             SwitchTerminalService switchTerminalService, SwitchTransactionService switchTransactionService,
                             AtmSessionService atmSessionService) {
        this.isoResponseFactory = isoResponseFactory;
        this.isoRequestValidator = isoRequestValidator;
        this.isoMessageParser = isoMessageParser;
        this.transactionRouter = transactionRouter;
        this.switchTerminalService = switchTerminalService;
        this.switchTransactionService = switchTransactionService;
        this.atmSessionService = atmSessionService;
    }

    public ISOMsg handle(ISOMsg request) {
        try {
            String mti = request.getMTI();

            log.info("Received ISO 8583 message with MTI: {}", mti);

            String validationCode = this.isoRequestValidator.validate(request);

            if (validationCode != null) {
                log.warn("ISO 8583 request rejected: MTI={}, responseCode={}", mti, validationCode);
                return this.isoResponseFactory.createResponse(request, validationCode);
            }

            if (request.hasField(3)) {
                log.info("Processing code={}", request.getString(3));
            }

            if (request.hasField(11)) {
                log.info("STAN={}", request.getString(11));
            }

            if (request.hasField(41)) {
                log.info("Terminal ID={}", request.getString(41));
            }

            String terminalId = request.getString(41);

            if (!this.switchTerminalService.isActive(terminalId)) {
                log.warn("Unknown or disabled terminal ID={}", terminalId);
                return this.isoResponseFactory.createResponse(request, "03");
            }

            String processingCode = request.getString(3);

            if ("0200".equals(mti) && ProcessingCodes.requiresSession(processingCode)) {
                String sessionToken = request.getString(62);
                String pan = request.getString(2);

                if (!this.atmSessionService.validateAndTouch(sessionToken, pan, terminalId)) {
                    log.warn("Transaction rejected: missing or invalid ATM session; terminalId={}, processingCode={}", terminalId, processingCode);
                    return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_SESSION);
                }
            }

            if ("0100".equals(mti)) {
                ISOMsg response = this.transactionRouter.route(request);

                if (response == null) {
                    return this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_TRANSACTION);
                }

                return response;
            }

            String amountField = request.getString(4);
            String currencyCode = request.getString(49);

            if (amountField == null || !amountField.matches("\\d{12}")
                    || currencyCode == null || !currencyCode.matches("\\d{3}")) {
                return this.isoResponseFactory.createResponse(request, ResponseCodes.FORMAT_ERROR);
            }

            BigDecimal transactionAmount = new BigDecimal(amountField).movePointLeft(2);

            ISOMsg sanitizedRequest = (ISOMsg) request.clone();
            sanitizedRequest.unset(52);
            sanitizedRequest.unset(62);

            byte[] requestPayload = this.isoMessageParser.pack(request);

            SwitchTransactionRegistrationResult registrationResult = this.switchTransactionService.registerRequest(terminalId,
                    request.getString(11), request.getString(37), request.getString(3), mti,
                    request.getString(7), transactionAmount, currencyCode, requestPayload
            );

            switch (registrationResult.outcome()) {
                case REQUEST_IDENTITY_CONFLICT -> {
                    log.warn("Transaction identity conflict: existingTransactionId={}, terminalId={}, stan={}", registrationResult.transactionId(), terminalId, request.getString(11));

                    return this.isoResponseFactory.createResponse(request, ResponseCodes.DUPLICATE_TRANSACTION);
                }

                case DUPLICATE_REQUEST -> {
                    log.warn("Duplicate transaction received, transactionId={}, terminalId={}, stan={}", registrationResult.transactionId(), terminalId, request.getString(11));

                    return this.switchTransactionService.findResponsePayload(registrationResult.transactionId())
                            .map(payload -> {
                                try {
                                    return this.isoMessageParser.unpack(payload);
                                } catch (ISOException e) {
                                    throw new IllegalStateException(
                                            "Could not unpack stored transaction response", e
                                    );
                                }
                            })
                            .orElseGet(() -> this.isoResponseFactory.createResponse(request, ResponseCodes.DUPLICATE_TRANSACTION));
                }

                case REGISTERED -> {
                    // Continue with normal transaction processing.
                }
            }

            long transactionId = registrationResult.transactionId();

            try {
                int processingStarted = this.switchTransactionService.markProcessing(transactionId);

                if (processingStarted != 1) {
                    log.warn("Could not start transaction {}; status is no longer RECEIVED", transactionId);

                    return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
                }

                ISOMsg response = this.transactionRouter.route(request);

                if (response == null) {
                    log.warn("Unsupported processing code={}", request.getString(3));
                    response = this.isoResponseFactory.createResponse(request, ResponseCodes.INVALID_TRANSACTION);
                }

                String responseCode = response.getString(39);

                TransactionStatus status = ResponseCodes.APPROVED.equals(responseCode) ? TransactionStatus.APPROVED : TransactionStatus.DECLINED;

                byte[] responsePayload = this.isoMessageParser.pack(response);

                TransactionCompletionResult completionResult = this.switchTransactionService.complete(transactionId, responseCode, status, responsePayload);

                if (completionResult.completed()) {
                    return response;
                }

                byte[] storedPayload = completionResult.responsePayload();

                if (storedPayload != null) {
                    return this.isoMessageParser.unpack(storedPayload);
                }

                log.error("Transaction {} has status {} but no stored response payload", transactionId, completionResult.currentStatus());

                return this.isoResponseFactory.createResponse(request, ResponseCodes.SYSTEM_ERROR);
            } catch (RuntimeException e) {
                log.error("Transaction processing failed; transactionId={}", transactionId, e);
                this.switchTransactionService.markFailed(transactionId, TransactionStatus.FAILED);
                throw e;
            }
        } catch (ISOException e) {
            log.error("Error occurred while handling ISO message", e);
            throw new RuntimeException("Error handling ISO message", e);
        }
    }
}
