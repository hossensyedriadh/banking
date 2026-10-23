package io.github.hossensyedriadh.atm_pos_switch.iso8583.validator;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ProcessingCodes;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class IsoRequestValidator {
    public String validate(ISOMsg request) throws ISOException {
        String mti = request.getMTI();

        if (!"0100".equals(mti) && !"0200".equals(mti) && !"0400".equals(mti)) {
            return ResponseCodes.INVALID_TRANSACTION;
        }

        if ("0100".equals(mti)) {
            return this.validatePinVerification(request);
        }

        if (!request.hasField(3) || !request.hasField(11) || !request.hasField(41)) {
            return ResponseCodes.FORMAT_ERROR;
        }

        if (!request.getString(3).matches("\\d{6}") || !request.getString(11).matches("\\d{6}")
                || request.getString(41).isBlank()) {
            return ResponseCodes.FORMAT_ERROR;
        }

        if ("0400".equals(mti)) {
            String originalDataElements = request.getString(90);

            if (originalDataElements == null || !originalDataElements.matches("\\d{42}")) {
                return ResponseCodes.FORMAT_ERROR;
            }
        }

        return null;
    }

    private String validatePinVerification(ISOMsg request) {
        if (!request.hasField(2) || !request.hasField(3) || !request.hasField(7)
                || !request.hasField(11) || !request.hasField(41) || !request.hasField(52)) {
            return ResponseCodes.FORMAT_ERROR;
        }

        String pan = request.getString(2);
        String processingCode = request.getString(3);
        String transmissionDateTime = request.getString(7);
        String stan = request.getString(11);
        String terminalId = request.getString(41);
        byte[] pinBlock = request.getBytes(52);

        if (pan == null || !pan.matches("\\d{12,19}")) {
            return ResponseCodes.FORMAT_ERROR;
        }

        if (!ProcessingCodes.PIN_VERIFICATION.equals(processingCode)) {
            return ResponseCodes.INVALID_TRANSACTION;
        }

        if (transmissionDateTime == null
                || !transmissionDateTime.matches("\\d{10}")) {
            return ResponseCodes.FORMAT_ERROR;
        }

        if (stan == null || !stan.matches("\\d{6}")) {
            return ResponseCodes.FORMAT_ERROR;
        }

        if (terminalId == null || terminalId.isBlank()) {
            return ResponseCodes.FORMAT_ERROR;
        }

        if (pinBlock == null || pinBlock.length != 8) {
            return ResponseCodes.FORMAT_ERROR;
        }

        return null;
    }
}
