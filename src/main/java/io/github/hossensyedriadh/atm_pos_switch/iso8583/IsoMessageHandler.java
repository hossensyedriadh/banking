package io.github.hossensyedriadh.atm_pos_switch.iso8583;

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

    @Autowired
    public IsoMessageHandler(IsoResponseFactory isoResponseFactory, IsoRequestValidator isoRequestValidator) {
        this.isoResponseFactory = isoResponseFactory;
        this.isoRequestValidator = isoRequestValidator;
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

            return this.isoResponseFactory.createResponse(request, "00");
        } catch (ISOException e) {
            log.error("Error occurred while handling ISO message", e);
            throw new RuntimeException("Error handling ISO message", e);
        }
    }
}
