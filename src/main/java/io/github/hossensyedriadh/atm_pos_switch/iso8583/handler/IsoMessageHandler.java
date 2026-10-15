package io.github.hossensyedriadh.atm_pos_switch.iso8583.handler;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.validator.IsoRequestValidator;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.response.IsoResponseFactory;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.router.TransactionRouter;
import io.github.hossensyedriadh.atm_pos_switch.terminal.api.service.SwitchTerminalService;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class IsoMessageHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final IsoRequestValidator isoRequestValidator;
    private final TransactionRouter transactionRouter;
    private final SwitchTerminalService switchTerminalService;

    @Autowired
    public IsoMessageHandler(IsoResponseFactory isoResponseFactory, IsoRequestValidator isoRequestValidator,
                             TransactionRouter transactionRouter, SwitchTerminalService switchTerminalService) {
        this.isoResponseFactory = isoResponseFactory;
        this.isoRequestValidator = isoRequestValidator;
        this.transactionRouter = transactionRouter;
        this.switchTerminalService = switchTerminalService;
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

            String terminalId = request.getString(41);

            if (!this.switchTerminalService.isActive(terminalId)) {
                log.warn("Unknown or disabled terminal ID={}", terminalId);
                return this.isoResponseFactory.createResponse(request, "03");
            }

            /*
             * ISO 8583 documentation: https://www.iso.org/standard/79451.html
             * */
            if (request.hasField(3)) {
                log.info("Processing code={}", request.getString(3));
            }

            if (request.hasField(11)) {
                log.info("STAN={}", request.getString(11));
            }

            if (request.hasField(41)) {
                log.info("Terminal ID={}", request.getString(41));
            }

            ISOMsg response = this.transactionRouter.route(request);

            if (response == null) {
                log.warn("Unsupported processing code={}", request.getString(3));
                return this.isoResponseFactory.createResponse(request, "12");
            }

            return response;
        } catch (ISOException e) {
            log.error("Error occurred while handling ISO message", e);
            throw new RuntimeException("Error handling ISO message", e);
        }
    }
}
