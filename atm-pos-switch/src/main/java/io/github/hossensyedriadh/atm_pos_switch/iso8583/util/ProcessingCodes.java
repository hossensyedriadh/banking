package io.github.hossensyedriadh.atm_pos_switch.iso8583.util;

public final class ProcessingCodes {
    private ProcessingCodes() {
    }

    public static final String CASH_WITHDRAWAL = "010000";
    public static final String FUND_TRANSFER = "400000";
    public static final String BALANCE_INQUIRY = "310000";
    public static final String PIN_VERIFICATION = "710000";
    public static final String SESSION_TERMINATION = "990001";

    public static boolean requiresSession(String processingCode) {
        return BALANCE_INQUIRY.equals(processingCode) || CASH_WITHDRAWAL.equals(processingCode) || FUND_TRANSFER.equals(processingCode);
    }
}
