package io.github.hossensyedriadh.atm_pos_switch.transaction;

import org.jpos.iso.ISOMsg;

public interface TransactionHandler {
    String processingCode();

    ISOMsg handle(ISOMsg request);
}
