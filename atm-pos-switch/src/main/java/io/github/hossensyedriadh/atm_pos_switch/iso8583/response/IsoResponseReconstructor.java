package io.github.hossensyedriadh.atm_pos_switch.iso8583.response;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.parser.IsoMessageParser;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.util.ResponseCodes;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IsoResponseReconstructor {
    private final IsoMessageParser isoMessageParser;
    private final IsoResponseFactory isoResponseFactory;

    @Autowired
    public IsoResponseReconstructor(IsoMessageParser isoMessageParser, IsoResponseFactory isoResponseFactory) {
        this.isoMessageParser = isoMessageParser;
        this.isoResponseFactory = isoResponseFactory;
    }

    public byte[] reconstructApprovedResponse(byte[] requestPayload) {
        try {
            ISOMsg request = this.isoMessageParser.unpack(requestPayload);
            ISOMsg response = this.isoResponseFactory.createResponse(request, ResponseCodes.APPROVED);

            return this.isoMessageParser.pack(response);
        } catch (ISOException e) {
            throw new IllegalStateException("Failed to reconstruct ISO response for reconciled transaction", e);
        }
    }
}
