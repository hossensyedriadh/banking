package io.github.hossensyedriadh.atm_pos_switch.iso8583.response;

import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class IsoResponseFactory {
    public ISOMsg createResponse(ISOMsg request, String responseCode) {
        try {
            ISOMsg response = new ISOMsg();

            String responseMti = switch (request.getMTI()) {
                case "0100" -> "0110";
                case "0200" -> "0210";
                case "0400" -> "0410";
                default -> throw new IllegalArgumentException("Unsupported request MTI: " + request.getMTI());
            };

            response.setMTI(responseMti);

            this.copyField(request, response, 3);
            this.copyField(request, response, 7);
            this.copyField(request, response, 11);
            this.copyField(request, response, 37);
            this.copyField(request, response, 41);
            this.copyField(request, response, 49);

            if ("0400".equals(request.getMTI())) {
                this.copyField(request, response, 90);
            }

            response.set(39, responseCode);

            return response;
        } catch (ISOException e) {
            log.error("Failed to create ISO response", e);
            throw new RuntimeException("Failed to create response", e);
        }
    }

    private void copyField(ISOMsg source, ISOMsg target, int fieldNumber) {
        if (source.hasField(fieldNumber)) {
            target.set(fieldNumber, source.getString(fieldNumber));
        }
    }
}
