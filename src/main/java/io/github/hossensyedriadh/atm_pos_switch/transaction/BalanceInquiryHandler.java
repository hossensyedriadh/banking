package io.github.hossensyedriadh.atm_pos_switch.transaction;

import io.github.hossensyedriadh.atm_pos_switch.iso8583.AdditionalAmountEncoder;
import io.github.hossensyedriadh.atm_pos_switch.iso8583.IsoResponseFactory;
import lombok.extern.log4j.Log4j2;
import org.jpos.iso.ISOMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class BalanceInquiryHandler implements TransactionHandler {
    private final IsoResponseFactory isoResponseFactory;
    private final AdditionalAmountEncoder additionalAmountEncoder;

    @Autowired
    public BalanceInquiryHandler(IsoResponseFactory isoResponseFactory, AdditionalAmountEncoder additionalAmountEncoder) {
        this.isoResponseFactory = isoResponseFactory;
        this.additionalAmountEncoder = additionalAmountEncoder;
    }

    @Override
    public String processingCode() {
        return "310000";
    }

    @Override
    public ISOMsg handle(ISOMsg request) {
        ISOMsg response = this.isoResponseFactory.createResponse(request, "00");

        String balance = this.additionalAmountEncoder.encode("00", "01",
                "050", 'C', 12_500);
        response.set(54, balance);

        return response;
    }
}
