package io.github.hossensyedriadh.atm_terminal.util;

public final class ResponseCodes {

    private ResponseCodes() {
    }

    public static final String APPROVED = "00";
    public static final String INVALID_TRANSACTION = "12";
    public static final String INVALID_AMOUNT = "13";
    public static final String INVALID_CARD = "14";
    public static final String FORMAT_ERROR = "30";
    public static final String INSUFFICIENT_FUNDS = "51";
    public static final String INVALID_ACCOUNT = "52";
    public static final String EXPIRED_CARD = "54";
    public static final String INVALID_PIN = "55";
    public static final String DEBIT_BLOCK = "57";
    public static final String CREDIT_BLOCK = "57";
    public static final String INVALID_CURRENCY = "57";
    public static final String INVALID_SESSION = "58";
    public static final String FULL_FREEZE = "62";
    public static final String RESTRICTED_CARD = "62";
    public static final String PIN_BLOCKED = "75";
    public static final String DUPLICATE_TRANSACTION = "94";
    public static final String SYSTEM_ERROR = "96";
}