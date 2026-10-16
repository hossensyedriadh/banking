package io.github.hossensyedriadh.atm_pos_switch.iso8583.validator;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Component
public class IsoRequestValidator {
    public String validate(ISOMsg request) throws ISOException {
        String mti = request.getMTI();

        if (!"0200".equals(mti) && !"0400".equals(mti)) {
            return "12";
        }

        if (!request.hasField(3) || !request.hasField(11) || !request.hasField(41)) {
            return "30";
        }

        if (!request.getString(3).matches("\\d{6}")) {
            return "30";
        }

        if (!request.getString(11).matches("\\d{6}")) {
            return "30";
        }

        if (request.getString(41).isBlank()) {
            return "30";
        }

        if ("0400".equals(mti)) {
            String originalDataElements = request.getString(90);

            if (originalDataElements == null || !originalDataElements.matches("\\d{42}")) {
                return "30";
            }
        }

        return null;
    }
}
